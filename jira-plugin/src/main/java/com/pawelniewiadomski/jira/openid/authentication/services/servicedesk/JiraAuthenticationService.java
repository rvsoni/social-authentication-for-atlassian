package com.pawelniewiadomski.jira.openid.authentication.services.servicedesk;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.google.common.collect.Iterables;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.atlassian.jira.component.ComponentAccessor.getComponentOfType;
import static com.pawelniewiadomski.AllowedDomains.isEmailFromAllowedDomain;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang.StringUtils.*;

@Slf4j
@Component
public class JiraAuthenticationService implements AuthenticationService {
    @Autowired protected UserManager userManager;

    @Autowired protected CrowdService crowdService;

    @Autowired protected GlobalSettings globalSettings;

    @Autowired protected TemplateHelper templateHelper;

    @Autowired protected ExternalUserManagementService externalUserManagementService;

    @Autowired protected RedirectionService redirectionService;

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                                   final OpenIdProvider provider, String identity, String email) throws IOException, ServletException {
        if (isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (isNotBlank(provider.getAllowedDomains())) {
            if (!isEmailFromAllowedDomain(provider, email)) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        final com.atlassian.crowd.embedded.api.User user = (com.atlassian.crowd.embedded.api.User) Iterables.getFirst(crowdService.search(new UserQuery(
                com.atlassian.crowd.embedded.api.User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                stripToEmpty(email).toLowerCase()), 0, 1)), null);

        final ApplicationUser appUser;
        if (user == null && !externalUserManagementService.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            try {
                final String userName = lowerCase(replaceChars(identity, " '()", ""));

                appUser = userManager.createUser(
                        new UserDetails(userName, identity)
                            .withPassword(randomUUID().toString())
                            .withEmail(email)
                );
            } catch (Exception e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        } else {
            appUser = ApplicationUsers.from(user);
        }

        if (user != null) {
            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            getComponentOfType(LoginManager.class).onLoginAttempt(request, appUser.getName(), true);

            getComponentOfType(RememberMeService.class).addRememberMeCookie(request, response, appUser.getUsername());

            redirectionService.redirectToReturnUrlOrHome(request, response);
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }
}

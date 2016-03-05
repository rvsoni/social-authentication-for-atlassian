package com.pawelniewiadomski.jira.openid.authentication.services.servicedesk;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.compatibility.bridge.user.UserUtilBridge;
import com.atlassian.jira.compatibility.factory.user.UserUtilBridgeFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.google.common.collect.Iterables;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.apache.commons.lang.StringUtils.replaceChars;

@Slf4j
@Component
public class JiraAuthenticationService implements AuthenticationService {
    @Autowired protected UserUtilBridgeFactory userUtilBridgeFactory;

    @Autowired protected CrowdService crowdService;

    @Autowired protected GlobalSettings globalSettings;

    @Autowired protected TemplateHelper templateHelper;

    @Autowired protected ExternalUserManagementService externalUserManagementService;

    @Autowired protected RedirectionService redirectionService;

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                                   final OpenIdProvider provider, String identity, String email) throws IOException, ServletException {
        if (StringUtils.isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (StringUtils.isNotBlank(provider.getAllowedDomains())) {
            final String[] allowedDomains = StringUtils.split(provider.getAllowedDomains(), ',');
            final String domain = StringUtils.substringAfter(email, "@");
            boolean matchingDomain = false;
            for (final String allowedDomain : allowedDomains) {
                if (StringUtils.equals(StringUtils.trim(allowedDomain), domain)) {
                    matchingDomain = true;
                    break;
                }
            }
            if (!matchingDomain) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        com.atlassian.crowd.embedded.api.User user = (com.atlassian.crowd.embedded.api.User) Iterables.getFirst(crowdService.search(new UserQuery(
                com.atlassian.crowd.embedded.api.User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);

        if (user == null && !externalUserManagementService.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            try {
                final UserUtilBridge userUtil = ((UserUtilBridge) userUtilBridgeFactory.getObject());
                user = ApplicationUsers.toDirectoryUser(userUtil.createUserNoNotification(lowerCase(replaceChars(identity, " '()", "")), randomUUID().toString(),
                        email, identity));
            } catch (Exception e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        }

        if (user != null) {
            final ApplicationUser appUser = ApplicationUsers.from(user);

            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            ComponentAccessor.getComponentOfType(LoginManager.class).onLoginAttempt(request, appUser.getName(), true);

            redirectionService.redirectToReturnUrlOrHome(request, response);
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }
}

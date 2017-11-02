package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.atlassian.spring.container.ContainerManager.getComponent;
import static com.atlassian.user.security.password.Credential.unencrypted;
import static com.pawelniewiadomski.AllowedDomains.isEmailFromAllowedDomain;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang.StringUtils.*;

@Slf4j
@Component
@AllArgsConstructor
public class ConfluenceAuthenticationService implements AuthenticationService {
    final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    final GroupManager groupManager;

    final UserAccessor userAccessor;

    final SettingsManager settingsManager;

    final ExternalUserManagementService externalUserManagementService;

    final RedirectionService redirectionService;

    final LoginManager loginManager;

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

        ConfluenceUser user = null;
        final SearchResult usersByEmail = userAccessor.getUsersByEmail(StringUtils.stripToEmpty(email).toLowerCase());

        if (usersByEmail.pager().isEmpty()
                && !externalUserManagementService.isExternalUserManagement()
                && globalSettings.isCreatingUsers()) {
            try {
                user = userAccessor.createUser(
                        new DefaultUser(lowerCase(replaceChars(identity, " '()", "")), identity, email),
                        unencrypted(randomUUID().toString()));

                final Group defaultGroup = groupManager
                        .getGroup(settingsManager.getGlobalSettings().getDefaultUsersGroup());
                groupManager.addMembership(defaultGroup, user);
            } catch (UnsupportedOperationException | IllegalArgumentException | EntityException e) {
                log.error(format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        } else if (!usersByEmail.pager().isEmpty()) {
            User crowdUser = (User) usersByEmail.pager().iterator().next();
            user = userAccessor.getUserByName(crowdUser.getName());
        }

        if (user != null) {
            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            loginManager.onSuccessfulLoginAttempt(user.getName(), request);

            getComponent("rememberMeService", RememberMeService.class).addRememberMeCookie(request, response, user.getName());

            redirectionService.redirectToReturnUrlOrHome(request, response);
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }
}

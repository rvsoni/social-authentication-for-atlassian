package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.exception.InfrastructureException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.impl.DuplicateEntityException;
import com.atlassian.user.search.SearchResult;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.ProvidedUserDetails;
import com.pawelniewiadomski.jira.openid.authentication.services.RedirectionService;
import com.pawelniewiadomski.jira.openid.authentication.services.TemplateHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

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

    public void showAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                   final OpenIdProvider provider, final ProvidedUserDetails userDetails) throws IOException, ServletException {
        if (isBlank(userDetails.getEmail())) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (isNotBlank(provider.getAllowedDomains())) {
            if (!isEmailFromAllowedDomain(provider, userDetails.getEmail())) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        ConfluenceUser user = getUserByEmail(userDetails);
        if (user == null && !externalUserManagementService.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            try {
                final String userName = lowerCase(replaceChars(userDetails.getIdentity(), " '()", ""));

                for(int i = 0; i < 10 && user == null; ++i) {
                    try {
                        val tryUserName = userName + (i == 0 ? "" : i);
                        user = userAccessor.createUser(
                                new DefaultUser(
                                        tryUserName,
                                        userDetails.getIdentity(),
                                        userDetails.getEmail()),
                                        unencrypted(randomUUID().toString())
                        );
                    } catch (InfrastructureException e) {
                        if (!(e.getCause() instanceof UserAlreadyExistsException || e.getCause() instanceof InvalidUserException || e.getCause() instanceof DuplicateEntityException)) {
                            throw e;
                        }
                    }
                }

                if (user != null) {
                    final Group defaultGroup = groupManager.getGroup(settingsManager.getGlobalSettings().getDefaultUsersGroup());
                    groupManager.addMembership(defaultGroup, user);
                }
            } catch (UnsupportedOperationException | IllegalArgumentException | EntityException e) {
                log.error(format("Cannot create an account for %s %s", userDetails.getIdentity(), userDetails.getEmail()), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
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

    private ConfluenceUser getUserByEmail(ProvidedUserDetails userDetails) {
        final SearchResult usersByEmail = userAccessor.getUsersByEmail(stripToEmpty(userDetails.getEmail()).toLowerCase());
        if (!usersByEmail.pager().isEmpty()) {
            User crowdUser = (User) usersByEmail.pager().iterator().next();
            return userAccessor.getUserByName(crowdUser.getName());
        }
        return null;
    }
}

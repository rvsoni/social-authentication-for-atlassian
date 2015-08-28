package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.TemplateHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.atlassian.user.security.password.Credential.unencrypted;
import static com.pawelniewiadomski.jira.openid.authentication.BaseUrlHelper.getBaseUrl;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang.StringUtils.*;

@Slf4j
@ConfluenceComponent
@AllArgsConstructor
public class ConfluenceAuthenticationService implements AuthenticationService
{
    final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    @ComponentImport
    final UserAccessor userAccessor;

    final ExternalUserManagementService externalUserManagementService;

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                            final OpenIdProvider provider, String identity, String email) throws IOException, ServletException
    {
        if (isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (isNotBlank(provider.getAllowedDomains())) {
            final String[] allowedDomains = split(provider.getAllowedDomains(), ',');
            final String domain = substringAfter(email, "@");
            boolean matchingDomain = false;
            for (final String allowedDomain : allowedDomains) {
                if (StringUtils.equals(trim(allowedDomain), domain)) {
                    matchingDomain = true;
                    break;
                }
            }
            if (!matchingDomain) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        ConfluenceUser user;
        final SearchResult usersByEmail = userAccessor.getUsersByEmail(StringUtils.stripToEmpty(email).toLowerCase());

        if (usersByEmail.pager().isEmpty()
                && !externalUserManagementService.isExternalUserManagement()
                && globalSettings.isCreatingUsers()) {
            try {
                user = userAccessor.createUser(
                        new DefaultUser(lowerCase(replaceChars(identity, " '()", "")), identity, email),
                        unencrypted(randomUUID().toString()));
            } catch (UnsupportedOperationException | IllegalArgumentException e) {
                log.error(format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        } else {
            user = (ConfluenceUser) usersByEmail.pager().iterator().next();
        }

        if (user != null) {
            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
//            ComponentAccessor.getComponentOfType(LoginManager.class).onLoginAttempt(request, appUser.getName(), true);

            final String returnUrl = (String) httpSession.getAttribute(RETURN_URL_SESSION);
            if (isNotBlank(returnUrl)) {
                response.sendRedirect(getBaseUrl(request) + returnUrl);
            } else {
                response.sendRedirect(getBaseUrl(request) + "/index.action");
            }
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }
}

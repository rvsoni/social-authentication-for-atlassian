package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.google.common.collect.Iterables;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.servlet.AbstractOpenIdServlet;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

import static com.pawelniewiadomski.jira.openid.authentication.servlet.BaseUrlHelper.getBaseUrl;

@Slf4j
@JiraComponent
@AllArgsConstructor
public class JiraAuthenticationService implements AuthenticationService {
    @ComponentImport
    final UserUtil userUtil;

    @ComponentImport
    final CrowdService crowdService;

    final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    final AbstractOpenIdServlet abstractOpenIdServlet;

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

        if (user == null && !abstractOpenIdServlet.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            try {
                user = userUtil.createUserNoNotification(StringUtils.lowerCase(StringUtils.replaceChars(identity, " '()", "")), UUID.randomUUID().toString(),
                        email, identity);
            } catch (PermissionException | CreateException e) {
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

            final String returnUrl = (String) httpSession.getAttribute(RETURN_URL_SESSION);
            if (StringUtils.isNotBlank(returnUrl)) {
                response.sendRedirect(getBaseUrl(request) + returnUrl);
            } else {
                response.sendRedirect(getBaseUrl(request) + "/secure/Dashboard.jspa");
            }
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }
}

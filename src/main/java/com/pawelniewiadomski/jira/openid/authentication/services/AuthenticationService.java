package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
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
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.google.common.collect.Iterables;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.servlet.AbstractOpenIdServlet;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

import static com.pawelniewiadomski.jira.openid.authentication.servlet.BaseUrlHelper.getBaseUrl;

public class AuthenticationService extends AbstractOpenIdServlet
{
    public static final String RETURN_URL_SESSION = AuthenticationService.class.getName() + ".returnUrl";
    public static final String STATE_IN_SESSION = AuthenticationService.class.getName() + ".state";
    public static final String RETURN_URL_PARAM = "returnUrl";

    final Logger log = Logger.getLogger(this.getClass());

    private final UserUtil userUtil;

    private final CrowdService crowdService;

    private final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    public AuthenticationService(UserUtil userUtil, CrowdService crowdService, GlobalSettings globalSettings,
                                 TemplateHelper templateHelper, OpenIdDao openIdDao) {
        super(openIdDao);
        this.userUtil = userUtil;
        this.crowdService = crowdService;
        this.globalSettings = globalSettings;
        this.templateHelper = templateHelper;
    }

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                            final OpenIdProvider provider, String identity, String email) throws IOException, ServletException
    {
        if (StringUtils.isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (StringUtils.isNotBlank(provider.getAllowedDomains())) {
            final String[] allowedDomains = StringUtils.split(provider.getAllowedDomains(), ',');
            final String domain = StringUtils.substringAfter(email, "@");
            boolean matchingDomain = false;
            for(final String allowedDomain : allowedDomains) {
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

        com.atlassian.crowd.embedded.api.User user = Iterables.<User>getFirst(crowdService.search(new UserQuery(
                com.atlassian.crowd.embedded.api.User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);

        if (user == null && !isExternalUserManagement()
                && (JiraUtils.isPublicMode() || globalSettings.isCreatingUsers())) {
            try {
                user = userUtil.createUserNoNotification(StringUtils.lowerCase(StringUtils.replaceChars(identity, " '()", "")), UUID.randomUUID().toString(),
                        email, identity);
            } catch (PermissionException e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            } catch (CreateException e) {
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

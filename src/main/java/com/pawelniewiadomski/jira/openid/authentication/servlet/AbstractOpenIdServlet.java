package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class AbstractOpenIdServlet extends HttpServlet {

	final OpenIdDao openIdDao;

    final ApplicationProperties applicationProperties;

    final LoginUriProvider loginUriProvider;

    final UserManager userManager;

    final I18nResolver i18nResolver;

    public AbstractOpenIdServlet(OpenIdDao openIdDao) {
        this.openIdDao = openIdDao;
        this.applicationProperties = ComponentLocator.getComponent(ApplicationProperties.class);
        this.loginUriProvider = ComponentLocator.getComponent(LoginUriProvider.class);
        this.userManager = ComponentLocator.getComponent(UserManager.class);
        this.i18nResolver = ComponentLocator.getComponent(I18nResolver.class);
    }

    protected boolean isExternalUserManagement() {
        try {
            com.atlassian.jira.config.properties.ApplicationProperties ap = ComponentLocator.getComponent(com.atlassian.jira.config.properties.ApplicationProperties.class);
            return ap != null && ap.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
        } catch (Throwable ignore) {
            // in case running in Confluence
            return false;
        }
    }

    protected boolean hasAdminPermission()
    {
        String user = userManager.getRemoteUsername();
        try
        {
            return user != null && (userManager.isAdmin(user) || userManager.isSystemAdmin(user));
        }
        catch(NoSuchMethodError e)
        {
            // userManager.isAdmin(String) was not added until SAL 2.1.
            // We need this check to ensure backwards compatibility with older product versions.
            return user != null && userManager.isSystemAdmin(user);
        }
    }

    void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}

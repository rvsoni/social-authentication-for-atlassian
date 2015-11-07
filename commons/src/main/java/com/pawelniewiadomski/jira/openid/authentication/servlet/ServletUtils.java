package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Service
public final class ServletUtils {

    @Autowired protected ApplicationProperties applicationProperties;

    @Autowired protected LoginUriProvider loginUriProvider;

    @Autowired protected UserManager userManager;

    @Autowired
    protected I18nResolver i18nResolver;

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

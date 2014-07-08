package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public class AbstractOpenIdServlet extends HttpServlet {

	@Autowired
	OpenIdDao openIdDao;

    @Autowired
    SoyTemplateRenderer soyTemplateRenderer;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    LoginUriProvider loginUriProvider;

    @Autowired
    UserManager userManager;

    @Autowired
    I18nResolver i18nResolver;

    public static final String SOY_TEMPLATES = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin:openid-soy-templates";

    String getBaseUrl(HttpServletRequest request) {
        return UriBuilder.fromUri(request.getRequestURL().toString()).replacePath(request.getContextPath()).build().toString();
    }

    boolean isExternalUserManagement() {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

    void renderTemplate(final HttpServletRequest request,
                        final HttpServletResponse response,
                        String template, Map<String, Object> map) throws ServletException, IOException {
        final Map<String, Object> params = Maps.newHashMap(map);
        params.put("baseUrl", getBaseUrl(request));

        JiraHttpUtils.setNoCacheHeaders(response);
        response.setContentType(getContentType());
        try {
            soyTemplateRenderer.render(response.getWriter(), SOY_TEMPLATES, template, params);
        } catch (SoyException e) {
            throw new ServletException(e);
        }
    }

    String getContentType()
    {
        try
        {
            return applicationProperties.getContentType();
        }
        catch (Exception e)
        {
            return "text/html; charset=UTF-8";
        }
    }

    boolean hasAdminPermission()
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

    String getReturnTo(OpenIdProvider provider, final HttpServletRequest request) {
        return UriBuilder.fromUri(getBaseUrl(request)).path("/plugins/servlet/openid-authentication")
                .queryParam("pid", provider.getID()).build().toString();
    }
}

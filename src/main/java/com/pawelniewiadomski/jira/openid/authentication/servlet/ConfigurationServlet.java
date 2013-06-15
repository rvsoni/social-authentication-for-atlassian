package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.storage.lib.AtlassianMarketplaceUriFactory;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigurationServlet extends AbstractOpenIdServlet
{
    @Autowired
    OpenIdDao openIdDao;

    @Autowired
    WebResourceManager webResourceManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (userManager.getRemoteUsername() == null)
        {
            redirectToLogin(req, resp);
            return;
        }
        else if (!hasAdminPermission())
        {
            throw new UnsupportedOperationException("you don't have permission");
        }

        final String operation = req.getParameter("op");
        final String providerId = req.getParameter("pid");
        if (StringUtils.isNotEmpty(providerId)) {
            try {
                final OpenIdProvider provider = openIdDao.findProvider(Integer.valueOf(providerId));
                if (provider != null) {
                    if (StringUtils.equals("delete", operation)) {

                    } else if (StringUtils.equals("edit", operation)) {

                    } else if (StringUtils.equals("disable", operation)) {
                        provider.setEnabled(false);
                        provider.save();
                    } else if (StringUtils.equals("enable", operation)) {
                        provider.setEnabled(true);
                        provider.save();
                    }
                }
                resp.sendRedirect(req.getRequestURI());
                return;
            } catch (SQLException e) {
                // ignore
            }
        }

        try {
            webResourceManager.requireResourcesForContext("jira-openid-configuration");
            renderTemplate(resp, "OpenId.Templates.providers",
                    ImmutableMap.<String, Object>of("providers", openIdDao.findAllProviders(),
                            "currentUrl", req.getRequestURI()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

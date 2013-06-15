package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@Component
public class ConfigurationServlet extends AbstractOpenIdServlet
{
    @Autowired
    OpenIdDao openIdDao;

    @Autowired
    WebResourceManager webResourceManager;

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final String operation = req.getParameter("op");
        if (StringUtils.equals("add", operation)) {
            final Map<String, Object> errors = Maps.newHashMap();
            final String name = req.getParameter("name");
            final String endpointUrl = req.getParameter("endpointUrl");
            final String extensionNamespace = req.getParameter("extensionNamespace");

            if (StringUtils.isEmpty(name)) {
                errors.put("name", )
            }
            provider = openIdDao.findByName(name);

            renderTemplate(resp, "OpenId.Templates.addProvider",
                    ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI(), "errors", errors));
            return;
        }
        resp.sendRedirect(req.getRequestURI());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (shouldNotAccess(req, resp)) return;

        webResourceManager.requireResourcesForContext("jira-openid-configuration");

        final String operation = req.getParameter("op");
        if (StringUtils.equals("add", operation)) {
            renderTemplate(resp, "OpenId.Templates.addProvider",
                    ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI()));
            return;
        }

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
            renderTemplate(resp, "OpenId.Templates.providers",
                    ImmutableMap.<String, Object>of("providers", openIdDao.findAllProviders(),
                            "currentUrl", req.getRequestURI()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldNotAccess(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (userManager.getRemoteUsername() == null)
        {
            redirectToLogin(req, resp);
            return true;
        }
        else if (!hasAdminPermission())
        {
            throw new UnsupportedOperationException("you don't have permission");
        }
        return false;
    }
}

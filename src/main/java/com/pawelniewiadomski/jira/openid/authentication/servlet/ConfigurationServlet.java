package com.pawelniewiadomski.jira.openid.authentication.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationServlet extends AbstractOpenIdServlet {
    @Autowired
    PageBuilderService pageBuilderService;

    @Autowired
    GlobalSettings globalSettings;

    @Autowired
    TemplateHelper templateHelper;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final WebResourceAssembler assembler = pageBuilderService.assembler();

        assembler.resources().requireContext("jira-openid-configuration");
        assembler.data()
                .requireData("openid.publicMode", JiraUtils.isPublicMode())
                .requireData("openid.creatingUsers", globalSettings.isCreatingUsers())
                .requireData("openid.externalUserManagement", isExternalUserManagement())
                .requireData("openid.baseUrl", applicationProperties.getString(APKeys.JIRA_BASEURL));

        templateHelper.render(req, resp, "OpenId.Templates.Configuration.container");
    }

    private boolean shouldNotAccess(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (userManager.getRemoteUsername() == null) {
            redirectToLogin(req, resp);
            return true;
        } else if (!hasAdminPermission()) {
            throw new UnsupportedOperationException("you don't have permission");
        }
        return false;
    }
}

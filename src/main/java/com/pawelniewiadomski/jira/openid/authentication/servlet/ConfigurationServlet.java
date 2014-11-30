package com.pawelniewiadomski.jira.openid.authentication.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.json.marshal.Jsonable;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;

import com.pawelniewiadomski.jira.openid.authentication.services.ProviderTypeFactory;
import org.codehaus.jackson.map.ObjectMapper;
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

    @Autowired
    ProviderTypeFactory providerTypeFactory;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final WebResourceAssembler assembler = pageBuilderService.assembler();

        assembler.resources().requireContext("jira-openid-configuration");
        assembler.data()
                .requireData("openid.publicMode", JiraUtils.isPublicMode())
                .requireData("openid.creatingUsers", globalSettings.isCreatingUsers())
                .requireData("openid.externalUserManagement", isExternalUserManagement())
                .requireData("openid.baseUrl", applicationProperties.getString(APKeys.JIRA_BASEURL))
                .requireData("openid.providerTypes", getProviderTypes());

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

    @Nonnull
    public Jsonable getProviderTypes() {
        final Collection<ProviderType> providers = providerTypeFactory.getAllProviderTypes().values();

        return new Jsonable() {
            @Override
            public void write(Writer writer) throws IOException {
                ObjectMapper om = new ObjectMapper();
                writer.write(om.writeValueAsString(providers));
            }
        };
    }
}

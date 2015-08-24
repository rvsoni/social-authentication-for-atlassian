package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.json.marshal.Jsonable;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.ProviderTypeFactory;
import lombok.AllArgsConstructor;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

@AllArgsConstructor
public class ConfigurationServlet extends HttpServlet {
    @ComponentImport
    final PageBuilderService pageBuilderService;

    final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    final ProviderTypeFactory providerTypeFactory;

    final AbstractOpenIdServlet abstractOpenIdServlet;

    final ApplicationProperties applicationProperties;

    final UserManager userManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final WebResourceAssembler assembler = pageBuilderService.assembler();

        assembler.resources().requireContext("jira-openid-configuration");
        assembler.data()
                .requireData("openid.publicMode", globalSettings.isJiraPublicMode())
                .requireData("openid.creatingUsers", globalSettings.isCreatingUsers())
                .requireData("openid.externalUserManagement", abstractOpenIdServlet.isExternalUserManagement())
                .requireData("openid.baseUrl", applicationProperties.getBaseUrl())
                .requireData("openid.providerTypes", getProviderTypes());

        templateHelper.render(req, resp, "OpenId.Templates.Configuration.container");
    }

    private boolean shouldNotAccess(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (userManager.getRemoteUsername() == null) {
            abstractOpenIdServlet.redirectToLogin(req, resp);
            return true;
        } else if (!abstractOpenIdServlet.hasAdminPermission()) {
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

package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.AllArgsConstructor;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class ConfigurationServlet extends HttpServlet {
    @Autowired
    protected PageBuilderService pageBuilderService;

    @Autowired
    protected GlobalSettings globalSettings;

    @Autowired
    protected TemplateHelper templateHelper;

    @Autowired
    protected ProviderTypeFactory providerTypeFactory;

    @Autowired
    protected ServletUtils servletUtils;

    @Autowired
    protected ApplicationProperties applicationProperties;

    @Autowired
    protected UserManager userManager;

    @Autowired
    protected PublicModeService publicModeService;

    @Autowired
    protected ExternalUserManagementService externalUserManagementService;

    @Autowired
    protected PluginKey pluginKey;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final WebResourceAssembler assembler = pageBuilderService.assembler();

        assembler.resources().requireContext(pluginKey.configurationContext());
        assembler.data()
                .requireData("openid.publicMode", publicModeService.canAnyoneSignUp())
                .requireData("openid.creatingUsers", globalSettings.isCreatingUsers())
                .requireData("openid.externalUserManagement", externalUserManagementService.isExternalUserManagement())
                .requireData("openid.baseUrl", applicationProperties.getBaseUrl())
                .requireData("openid.providerTypes", getProviderTypes());

        templateHelper.render(req, resp, "OpenId.Templates.Configuration.container");
    }

    private boolean shouldNotAccess(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (userManager.getRemoteUsername() == null) {
            servletUtils.redirectToLogin(req, resp);
            return true;
        } else if (!servletUtils.hasAdminPermission()) {
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

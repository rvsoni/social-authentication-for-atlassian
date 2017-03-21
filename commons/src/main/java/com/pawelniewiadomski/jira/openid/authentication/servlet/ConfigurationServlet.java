package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.JsonableUtil.toJsonable;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;

@Slf4j
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
    protected BaseUrlService baseUrlService;

    @Autowired
    protected UserManager userManager;

    @Autowired
    protected PublicModeService publicModeService;

    @Autowired
    protected ExternalUserManagementService externalUserManagementService;

    @Autowired
    protected PluginKey pluginKey;

    protected void configureAssembler(WebResourceAssembler assembler) {
        // left blank
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        final WebResourceAssembler assembler = pageBuilderService.assembler();

        final Map<String, Object> openIdData = prepareData(req);

        assembler.resources().requireContext(pluginKey.configurationContext());

        openIdData.entrySet().forEach(ev -> assembler.data().requireData("openid." + ev.getKey(),
                toJsonable(ev.getValue())));

        assembler.data().requireData("openid.data", toJsonable(openIdData));

        StringWriter sw = new StringWriter();
        toJsonable(openIdData).write(sw);
        log.warn(sw.toString());

        configureAssembler(assembler);

        templateHelper.render(req, resp, "OpenId.Templates.Configuration.container");
    }

    private Map<String, Object> prepareData(HttpServletRequest req) {
        final String baseUrl = baseUrlService.getBaseUrl();

        ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();

        mapBuilder.put("publicMode", publicModeService.canAnyoneSignUp());
        mapBuilder.put("creatingUsers", globalSettings.isCreatingUsers());
        mapBuilder.put("externalUserManagement", externalUserManagementService.isExternalUserManagement());
        mapBuilder.put("baseUrl", baseUrl + '/' + pluginKey.getRestKey());
        mapBuilder.put("providerTypes", providerTypeFactory.getAllProviderTypes().values());
        mapBuilder.put("sessionTimeout", req.getSession().getMaxInactiveInterval() / 60);
        mapBuilder.put("sslMismatch", isBehindHttps(req, baseUrl));
        mapBuilder.put("sslDocumentation", pluginKey.getSslConfigurationTutorial());

        return mapBuilder.build();
    }

    private boolean isBehindHttps(HttpServletRequest req, String baseUrl) {
        return equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"), "https")
                && startsWithIgnoreCase(baseUrl, "http:");
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
}

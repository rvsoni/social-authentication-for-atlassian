package com.pawelniewiadomski.jira.openid.authentication.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;
import com.pawelniewiadomski.jira.openid.authentication.YahooProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class ConfigurationServlet extends AbstractOpenIdServlet {
    @Autowired
    PageBuilderService pageBuilderService;

    @Autowired
    GlobalSettings globalSettings;

    @Autowired
    TemplateHelper templateHelper;

    @Autowired
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    private Map<String, String> providerValuesMap(String name, String endpointUrl, String extensionNamespace, String allowedDomains,
                                                  String providerType, String clientId, String clientSecret) {
        final Map<String, String> results = Maps.newHashMap();
        results.put("name", name);
        results.put("endpointUrl", endpointUrl);
        results.put("allowedDomains", allowedDomains);
        results.put("providerType", providerType);

        if (providerType.equals(OpenIdProvider.OPENID_TYPE)) {
            results.put("extensionNamespace", extensionNamespace);
        } else {
            results.put("clientId", clientId);
            results.put("clientSecret", clientSecret);
        }
        return results;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        pageBuilderService.assembler().resources().requireContext("jira-openid-configuration");

        templateHelper.render(req, resp, "OpenId.Templates.Configuration.container",
                ImmutableMap.<String, Object>builder()
                        .put("publicMode", JiraUtils.isPublicMode())
                        .put("creatingUsers", globalSettings.isCreatingUsers())
                        .put("externalUserManagement", isExternalUserManagement())
                        .build());
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

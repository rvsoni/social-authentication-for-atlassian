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

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

        pageBuilderService.assembler().resources().requireContext("jira-openid-configuration");

        final String operation = req.getParameter("op");
        if (StringUtils.equals(operation, "delete")) {
            if (req.getParameterMap().containsKey("confirm")) {
                final String pid = req.getParameter("pid");
                try {
                    openIdDao.deleteProvider(Integer.valueOf(pid));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (StringUtils.equals(operation, "edit") || StringUtils.equals(operation, "add")) {
            final Map<String, Object> errors = Maps.newHashMap();
            final String name = req.getParameter("name");
            final String endpointUrl = req.getParameter("endpointUrl");
            final String extensionNamespace = req.getParameter("extensionNamespace");
            final String allowedDomains = req.getParameter("allowedDomains");
            final String providerType = req.getParameter("providerType");
            final String clientId = req.getParameter("clientId");
            final String clientSecret = req.getParameter("clientSecret");
            final String pid = req.getParameter("pid");
            final String callbackId = getCallbackId(req);
            final OpenIdProvider provider;
            try {
                provider = StringUtils.isNotEmpty(pid) ? openIdDao.findProvider(Integer.valueOf(pid)) : null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (StringUtils.isEmpty(name)) {
                errors.put("name", i18nResolver.getText("configuration.name.empty"));
            } else {
                final OpenIdProvider providerByName;
                try {
                    providerByName = openIdDao.findByName(name);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if (providerByName != null && (provider == null || provider.getID() != providerByName.getID())) {
                    errors.put("name", i18nResolver.getText("configuration.name.must.be.unique"));
                }
            }

            if (StringUtils.isEmpty(endpointUrl)) {
                errors.put("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
            }

            if (providerType.equals(OpenIdProvider.OAUTH2_TYPE)) {
                if (StringUtils.isEmpty(clientId)) {
                    errors.put("clientId", i18nResolver.getText("configuration.clientId.empty"));
                }
                if (StringUtils.isEmpty(clientSecret)) {
                    errors.put("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
                }
            } else {
                if (StringUtils.isEmpty(extensionNamespace)) {
                    errors.put("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
                }
            }

            if (!errors.isEmpty()) {
                final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
                mapBuilder.put("currentUrl", req.getRequestURI())
                        .put("errors", errors)
                        .put("presets", getPresets())
                        .put("callbackId", callbackId)
                        .put("callbackUrl", getCallbackUrl(callbackId))
                        .put("values", providerValuesMap(name, endpointUrl, extensionNamespace, allowedDomains,
                                providerType, clientId, clientSecret));

                if (StringUtils.isNotEmpty(pid)) {
                    mapBuilder.put("pid", pid);
                }

                templateHelper.render(req, resp,
                        provider != null ? "OpenId.Templates.editProvider" : "OpenId.Templates.addProvider",
                        mapBuilder.build());
                return;
            } else {
                try {
                    if (provider != null) {
                        provider.setName(name);
                        provider.setEndpointUrl(endpointUrl);
                        provider.setProviderType(providerType);
                        if (providerType.equals(OpenIdProvider.OAUTH2_TYPE)) {
                            provider.setClientId(clientId);
                            provider.setClientSecret(clientSecret);
                            if (isEmpty(provider.getCallbackId()))
                            {
                                provider.setCallbackId(getCallbackId(req));
                            }
                        } else {
                            provider.setExtensionNamespace(extensionNamespace);
                        }
                        provider.setAllowedDomains(allowedDomains);
                        provider.save();
                    } else {
                        openIdDao.createProvider(name, endpointUrl, extensionNamespace);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        resp.sendRedirect(req.getRequestURI());
    }

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

        final String operation = req.getParameter("op");
        if (StringUtils.equals("add", operation)) {
            final String callbackId = getCallbackId(req);
            templateHelper.render(req, resp, "OpenId.Templates.addProvider",
                    ImmutableMap.of(
                            "currentUrl", req.getRequestURI(),
                            "callbackId", callbackId,
                            "callbackUrl", getCallbackUrl(callbackId),
                            "presets", getPresets()));
            return;
        } else if (StringUtils.equals("onlyAuthenticate", operation)) {
            globalSettings.setCreatingUsers(false);
            resp.sendRedirect(req.getRequestURI());
            return;
        } else if (StringUtils.equals("createUsers", operation)) {
            globalSettings.setCreatingUsers(true);
            resp.sendRedirect(req.getRequestURI());
            return;
        }

        final String providerId = req.getParameter("pid");
        if (StringUtils.isNotEmpty(providerId)) {
            try {

                final OpenIdProvider provider = openIdDao.findProvider(Integer.valueOf(providerId));
                if (provider != null) {
                    if (StringUtils.equals("delete", operation)) {
                        templateHelper.render(req, resp, "OpenId.Templates.deleteProvider",
                                ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI(),
                                        "pid", providerId,
                                        "name", provider.getName()));
                        return;
                    } else if (StringUtils.equals("edit", operation)) {
                        final String callbackId = getCallbackId(provider);
                        templateHelper.render(req, resp, "OpenId.Templates.editProvider",
                                ImmutableMap.of(
                                        "currentUrl", req.getRequestURI(),
                                        "presets", getPresets(),
                                        "callbackId", callbackId,
                                        "callbackUrl", getCallbackUrl(callbackId),
                                        "values", providerValuesMap(provider.getName(),
                                                provider.getEndpointUrl(), provider.getExtensionNamespace(),
                                                provider.getAllowedDomains(), provider.getProviderType(),
                                                provider.getClientId(), provider.getClientSecret())));
                        return;
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

        templateHelper.render(req, resp, "OpenId.Templates.providers",
                ImmutableMap.<String, Object>builder()
                        .put("isPublic", JiraUtils.isPublicMode())
                        .put("isCreatingUsers", globalSettings.isCreatingUsers())
                        .put("isExternal", isExternalUserManagement())
                        .put("currentUrl", req.getRequestURI())
                        .build());
    }

    @Nonnull
    private String getCallbackUrl(@Nonnull String callbackId)
    {
        return applicationProperties.getString(APKeys.JIRA_BASEURL) + "/openid/oauth2-callback/" + callbackId;
    }

    @Nonnull
    private String getCallbackId(OpenIdProvider p)
    {
        return defaultIfEmpty(p.getCallbackId(), UUID.randomUUID().toString());
    }

    @Nonnull
    private String getCallbackId(HttpServletRequest req)
    {
        return defaultIfEmpty(req.getParameter("callbackId"), UUID.randomUUID().toString());
    }

    private List<Map<String, String>> getPresets()
    {
        final List<Map<String, String>> providers = Lists.newArrayList();
        for(OAuthProviderType provider : OAuthProviderType.values()) {
            providers.add(ImmutableMap.of(
                    "name", provider.getProviderName(),
                    "endpointUrl", provider.getAuthzEndpoint(),
                    "providerType", "oauth2"));
        }
        providers.add(ImmutableMap.of(
                "name", YahooProvider.NAME,
                "endpointUrl", YahooProvider.ENDPOINT_URL,
                "extensionNamespace", YahooProvider.EXTENSION_NAMESPACE,
                "providerType", "openid"));
        return Ordering.from(new Comparator<Map<String, String>>()
        {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2)
            {
                return o1.get("name").compareTo(o2.get("name"));
            }
        }).immutableSortedCopy(providers);
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

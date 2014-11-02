package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Supplier;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Path("providers")
@Produces({MediaType.APPLICATION_JSON})
public class ProvidersResource extends OpenIdResource {

    @Autowired
    OpenIdDao openIdDao;

    @Autowired
    I18nResolver i18nResolver;

    @Autowired
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @POST
    public Response createProvider(final ProviderBean providerBean) {
        return permissionDeniedIfNotAdmin().getOrElse(
                new Supplier<javax.ws.rs.core.Response>() {
                    @Override
                    public javax.ws.rs.core.Response get() {
                        OpenIdProvider provider = null;
                        ErrorCollection errors = new SimpleErrorCollection();

                        if (isEmpty(providerBean.getName())) {
                            errors.addError("name", i18nResolver.getText("configuration.name.empty"));
                        } else {
                            final OpenIdProvider providerByName;
                            try {
                                providerByName = openIdDao.findByName(providerBean.getName());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                            if (providerByName != null) {
                                errors.addError("name", i18nResolver.getText("configuration.name.must.be.unique"));
                            }
                        }

                        if (isEmpty(providerBean.getEndpointUrl())) {
                            errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
                        }

                        if (providerBean.getProviderType().equals(OpenIdProvider.OAUTH2_TYPE)) {
                            if (isEmpty(providerBean.getEndpointUrl())) {
                                errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty", providerBean.getEndpointUrl()));
                            } else {
                                try {
                                    discoveryDocumentProvider.getDiscoveryDocument(providerBean.getEndpointUrl());
                                } catch (Exception e) {
                                    errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.discovery.missing", providerBean.getEndpointUrl()));
                                }
                            }
                            if (isEmpty(providerBean.getClientId())) {
                                errors.addError("clientId", i18nResolver.getText("configuration.clientId.empty"));
                            }
                            if (isEmpty(providerBean.getClientSecret())) {
                                errors.addError("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
                            }
                        } else {
                            if (isEmpty(providerBean.getExtensionNamespace())) {
                                errors.addError("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
                            }
                        }

                        if (errors.hasAnyErrors()) {
                            return Response.ok(errors).build();
                        } else {
                            try {
                                if (provider == null) {
                                    provider = openIdDao.createProvider(providerBean.getName(),
                                            providerBean.getEndpointUrl(), providerBean.getExtensionNamespace());
                                }

                                provider.setName(providerBean.getName());
                                provider.setEndpointUrl(providerBean.getEndpointUrl());
                                provider.setProviderType(providerBean.getProviderType());
                                if (provider.getProviderType().equals(OpenIdProvider.OAUTH2_TYPE)) {
                                    provider.setClientId(providerBean.getClientId());
                                    provider.setClientSecret(providerBean.getClientSecret());
                                } else {
                                    provider.setExtensionNamespace(providerBean.getExtensionNamespace());
                                }
                                provider.setAllowedDomains(providerBean.getAllowedDomains());
                                provider.save();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                            return Response.ok(new ProviderBean(provider)).build();
                        }
                    }
                }
        );
    }

    @DELETE
    public Response deleteProvider(final Integer pid) {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<javax.ws.rs.core.Response>() {
            @Override
            public Response get() {
                try {
                    openIdDao.deleteProvider(pid);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return Response.noContent().build();
            }
        });
    }

//    @Override
//    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
//        if (shouldNotAccess(req, resp)) return;
//
//        pageBuilderService.assembler().resources().requireContext("jira-openid-configuration");
//
//        final String operation = req.getParameter("op");
//        if (StringUtils.equals(operation, "delete")) {
//            if (req.getParameterMap().containsKey("confirm")) {
//                final String pid = req.getParameter("pid");
//                try {
//                    openIdDao.deleteProvider(Integer.valueOf(pid));
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        } else if (StringUtils.equals(operation, "edit") || StringUtils.equals(operation, "add")) {
//            final Map<String, Object> errors = Maps.newHashMap();
//            final String name = req.getParameter("name");
//            final String endpointUrl = req.getParameter("endpointUrl");
//            final String extensionNamespace = req.getParameter("extensionNamespace");
//            final String allowedDomains = req.getParameter("allowedDomains");
//            final String providerType = req.getParameter("providerType");
//            final String clientId = req.getParameter("clientId");
//            final String clientSecret = req.getParameter("clientSecret");
//            final String pid = req.getParameter("pid");
//            final String callbackId = getCallbackId(req);
//            OpenIdProvider provider;
//            try {
//                provider = StringUtils.isNotEmpty(pid) ? openIdDao.findProvider(Integer.valueOf(pid)) : null;
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (StringUtils.isEmpty(name)) {
//                errors.addError("name", i18nResolver.getText("configuration.name.empty"));
//            } else {
//                final OpenIdProvider providerByName;
//                try {
//                    providerByName = openIdDao.findByName(name);
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//
//                if (providerByName != null && (provider == null || provider.getID() != providerByName.getID())) {
//                    errors.put("name", i18nResolver.getText("configuration.name.must.be.unique"));
//                }
//            }
//
//            if (StringUtils.isEmpty(endpointUrl)) {
//                errors.put("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
//            }
//
//            if (providerType.equals(OpenIdProvider.OAUTH2_TYPE)) {
//                if (StringUtils.isEmpty(endpointUrl)) {
//                    errors.put("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty", endpointUrl));
//                } else {
//                    try {
//                        discoveryDocumentProvider.getDiscoveryDocument(endpointUrl);
//                    } catch (Exception e) {
//                        errors.put("endpointUrl", i18nResolver.getText("configuration.endpointUrl.discovery.missing", endpointUrl));
//                    }
//                }
//                if (StringUtils.isEmpty(clientId)) {
//                    errors.put("clientId", i18nResolver.getText("configuration.clientId.empty"));
//                }
//                if (StringUtils.isEmpty(clientSecret)) {
//                    errors.put("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
//                }
//            } else {
//                if (StringUtils.isEmpty(extensionNamespace)) {
//                    errors.put("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
//                }
//            }
//
//            if (!errors.isEmpty()) {
//                final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
//                mapBuilder.put("currentUrl", req.getRequestURI())
//                        .put("errors", errors)
//                        .put("presets", getPresets())
//                        .put("callbackId", callbackId)
//                        .put("callbackUrl", getCallbackUrl(callbackId))
//                        .put("values", providerValuesMap(name, endpointUrl, extensionNamespace, allowedDomains,
//                                providerType, clientId, clientSecret));
//
//                if (StringUtils.isNotEmpty(pid)) {
//                    mapBuilder.put("pid", pid);
//                }
//
//                templateHelper.render(req, resp,
//                        provider != null ? "OpenId.Templates.editProvider" : "OpenId.Templates.addProvider",
//                        mapBuilder.build());
//                return;
//            } else {
//                try {
//                    if (provider == null)
//                    {
//                        provider = openIdDao.createProvider(name, endpointUrl, extensionNamespace);
//                    }
//
//                    provider.setName(name);
//                    provider.setEndpointUrl(endpointUrl);
//                    provider.setProviderType(providerType);
//                    if (providerType.equals(OpenIdProvider.OAUTH2_TYPE)) {
//                        provider.setClientId(clientId);
//                        provider.setClientSecret(clientSecret);
//                        if (isEmpty(provider.getCallbackId()))
//                        {
//                            provider.setCallbackId(getCallbackId(req));
//                        }
//                    } else {
//                        provider.setExtensionNamespace(extensionNamespace);
//                    }
//                    provider.setAllowedDomains(allowedDomains);
//                    provider.save();
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        resp.sendRedirect(req.getRequestURI());
//    }

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        if (shouldNotAccess(req, resp)) return;
//
//        pageBuilderService.assembler().resources().requireContext("jira-openid-configuration");
//
//        final String operation = req.getParameter("op");
//        if (StringUtils.equals("add", operation)) {
//            final String callbackId = getCallbackId(req);
//            templateHelper.render(req, resp, "OpenId.Templates.addProvider",
//                    ImmutableMap.of(
//                            "currentUrl", req.getRequestURI(),
//                            "callbackId", callbackId,
//                            "callbackUrl", getCallbackUrl(callbackId),
//                            "presets", getPresets()));
//            return;
//        } else if (StringUtils.equals("onlyAuthenticate", operation)) {
//            globalSettings.setCreatingUsers(false);
//            resp.sendRedirect(req.getRequestURI());
//            return;
//        } else if (StringUtils.equals("createUsers", operation)) {
//            globalSettings.setCreatingUsers(true);
//            resp.sendRedirect(req.getRequestURI());
//            return;
//        }
//
//        final String providerId = req.getParameter("pid");
//        if (StringUtils.isNotEmpty(providerId)) {
//            try {
//
//                final OpenIdProvider provider = openIdDao.findProvider(Integer.valueOf(providerId));
//                if (provider != null) {
//                    if (StringUtils.equals("delete", operation)) {
//                        templateHelper.render(req, resp, "OpenId.Templates.deleteProvider",
//                                ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI(),
//                                        "pid", providerId,
//                                        "name", provider.getName()));
//                        return;
//                    } else if (StringUtils.equals("edit", operation)) {
//                        final String callbackId = getCallbackId(provider);
//                        templateHelper.render(req, resp, "OpenId.Templates.editProvider",
//                                ImmutableMap.of(
//                                        "currentUrl", req.getRequestURI(),
//                                        "presets", getPresets(),
//                                        "callbackId", callbackId,
//                                        "callbackUrl", getCallbackUrl(callbackId),
//                                        "values", providerValuesMap(provider.getName(),
//                                                provider.getEndpointUrl(), provider.getExtensionNamespace(),
//                                                provider.getAllowedDomains(), provider.getProviderType(),
//                                                provider.getClientId(), provider.getClientSecret())));
//                        return;
//                    } else if (StringUtils.equals("disable", operation)) {
//                        provider.setEnabled(false);
//                        provider.save();
//                    } else if (StringUtils.equals("enable", operation)) {
//                        provider.setEnabled(true);
//                        provider.save();
//                    }
//                }
//                resp.sendRedirect(req.getRequestURI());
//                return;
//            } catch (SQLException e) {
//                // ignore
//            }
//        }
//
//        templateHelper.render(req, resp, "OpenId.Templates.providers",
//                ImmutableMap.<String, Object>builder()
//                        .put("isPublic", JiraUtils.isPublicMode())
//                        .put("isCreatingUsers", globalSettings.isCreatingUsers())
//                        .put("isExternal", isExternalUserManagement())
//                        .put("currentUrl", req.getRequestURI())
//                        .build());
//    }
}

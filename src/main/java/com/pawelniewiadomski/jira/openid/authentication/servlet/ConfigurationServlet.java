package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.*;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.LoadDefaultProvidersComponent;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ConfigurationServlet extends AbstractOpenIdServlet
{
    @Autowired
    WebResourceManager webResourceManager;

    @Autowired
    GlobalSettings globalSettings;

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (shouldNotAccess(req, resp)) return;

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
			final String pid = req.getParameter("pid");
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

				if (providerByName != null
						&& (provider == null || (provider != null && !provider.getId().equals(providerByName.getId())))) {
					errors.put("name", i18nResolver.getText("configuration.name.must.be.unique"));
				}
			}

			if (StringUtils.isEmpty(endpointUrl)) {
				errors.put("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
			}

			if (!errors.isEmpty()) {
				final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
				mapBuilder.put("currentUrl", req.getRequestURI())
						.put("errors", errors)
						.put("values", providerValuesMap(name, endpointUrl, extensionNamespace));

				if (StringUtils.isNotEmpty(pid)) {
					mapBuilder.put("pid", pid);
				}

				renderTemplate(req, resp,
						provider != null ? "OpenId.Templates.editProvider" : "OpenId.Templates.addProvider",
						mapBuilder.build());
				return;
			} else {
				try {
					if (provider != null) {
						provider.setName(name);
						provider.setEndpointUrl(endpointUrl);
						provider.setExtensionNamespace(extensionNamespace);
                        provider.setAllowedDomains(allowedDomains);
						provider.save();
					} else {
						openIdDao.createProvider(name, endpointUrl, extensionNamespace);
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		} else if (StringUtils.equals("allowedDomains", operation)) {
            OpenIdProvider provider = null;
            try {
                provider = openIdDao.findByName(LoadDefaultProvidersComponent.GOOGLE);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            provider.setAllowedDomains(req.getParameter("allowedDomains"));
            provider.save();
        }

        resp.sendRedirect(req.getRequestURI());
    }

	private ImmutableMap<String, String> providerValuesMap(String name, String endpointUrl, String extensionNamespace) {
		return ImmutableMap.of("name", name,
				"endpointUrl", endpointUrl,
				"extensionNamespace", extensionNamespace);
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (shouldNotAccess(req, resp)) return;

        webResourceManager.requireResourcesForContext("jira-openid-configuration");

        final String operation = req.getParameter("op");
        if (StringUtils.equals("add", operation)) {
            renderTemplate(req, resp, "OpenId.Templates.addProvider",
                    ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI()));
            return;
        } else if (StringUtils.equals("onlyAuthenticate", operation)) {
            globalSettings.setCreatingUsers(false);
            resp.sendRedirect(req.getRequestURI());
            return;
        } else if (StringUtils.equals("createUsers", operation)) {
            globalSettings.setCreatingUsers(true);
            resp.sendRedirect(req.getRequestURI());
            return;
        } else if (StringUtils.equals("advanced", operation)) {
            globalSettings.setAdvanced(!Boolean.valueOf(req.getParameter("value")));
            resp.sendRedirect(req.getRequestURI());
            return;
        }

        final String providerId = req.getParameter("pid");
        if (StringUtils.isNotEmpty(providerId)) {
            try {

                final OpenIdProvider provider = openIdDao.findProvider(Integer.valueOf(providerId));
                if (provider != null) {
                    if (StringUtils.equals("delete", operation) && !provider.isInternal()) {
						renderTemplate(req, resp, "OpenId.Templates.deleteProvider",
								ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI(),
										"pid", providerId,
										"name", provider.getName()));
						return;
                    } else if (StringUtils.equals("edit", operation) && !provider.isInternal()) {
						renderTemplate(req, resp, "OpenId.Templates.editProvider",
								ImmutableMap.<String, Object>of("currentUrl", req.getRequestURI(),
										"pid", providerId,
										"values", providerValuesMap(provider.getName(),
										provider.getEndpointUrl(), provider.getExtensionNamespace())));
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

        try {
            renderTemplate(req, resp, "OpenId.Templates.providers",
                    ImmutableMap.<String, Object>builder()
                            .put("providers", isAdvanced()
                                    ? getOrderedListOfProviders(openIdDao.findAllProviders())
                                    : ImmutableList.of(openIdDao.findByName(LoadDefaultProvidersComponent.GOOGLE)))
                            .put("isAdvanced", isAdvanced())
                            .put("isPublic", JiraUtils.isPublicMode())
                            .put("isCreatingUsers", globalSettings.isCreatingUsers())
                            .put("isExternal", isExternalUserManagement())
                            .put("currentUrl", req.getRequestURI())
                            .put("isSimpleAvailable", isSimpleAvailable()).build());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAdvanced() {
        return globalSettings.isAdvanced() || !isSimpleAvailable();
    }

    private boolean isSimpleAvailable() {
        List<OpenIdProvider> providers = null;
        try {
            providers = openIdDao.findAllEnabledProviders();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return providers.size() == 1 && StringUtils.equals(providers.get(0).getName(), LoadDefaultProvidersComponent.GOOGLE);
    }

    public static ImmutableList<OpenIdProvider> getOrderedListOfProviders(Iterable<OpenIdProvider> providers) throws SQLException {
		return Ordering.from(new Comparator<OpenIdProvider>() {
			@Override
			public int compare(OpenIdProvider o1, OpenIdProvider o2) {
				return o1.getName().compareTo(o2.getName());
			}
		}).immutableSortedCopy(providers);
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

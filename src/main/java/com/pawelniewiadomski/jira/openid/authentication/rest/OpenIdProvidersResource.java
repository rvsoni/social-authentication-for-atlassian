package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.fugue.Option;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.BasicProviderResponse;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("openIdProviders")
@Produces({MediaType.APPLICATION_JSON})
public class OpenIdProvidersResource extends OpenIdResource {
    private static final Logger log = Logger.getLogger(OpenIdProvidersResource.class);

    @Autowired
    OpenIdDao openIdDao;

    @GET
    @AnonymousAllowed
    @Path("/login")
    public Response getLogin() {
        try {
            final List<BasicProviderResponse> loginProviders = Lists.newArrayList(
                    Iterables.transform(openIdDao.findAllEnabledProviders(),
                    new Function<OpenIdProvider, BasicProviderResponse>() {
                        @Override
                        public BasicProviderResponse apply(@Nullable final OpenIdProvider input) {
                            return new BasicProviderResponse(input.getID(), input.getName(), input.getProviderType());
                        }
                    }));

            return Response.ok(loginProviders).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Response getProvidersResponse() {
        try {
            return Response.ok(Lists.newArrayList(
                    Iterables.transform(openIdDao.findAllProviders(),
                    new Function<OpenIdProvider, BasicProviderResponse>() {
                        @Override
                        public BasicProviderResponse apply(@Nullable final OpenIdProvider input) {
                            return new ProviderResponse(input.getID(), input.getName(),
                                    input.isEnabled(), input.getOrdering(), input.getAllowedDomains(), input.getProviderType());
                        }
                    }))).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    public Response getOpenIdProviders() {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<Response>() {
            @Override
            public Response get() {
                return getProvidersResponse();
            }
        });
    }

    @POST
    @Path("/moveUp/{providerId}")
    public Response moveProviderUp(@PathParam("providerId") final int providerId) {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<Response>() {
            @Override
            public Response get() {
                try {
                    final List<OpenIdProvider> providers = openIdDao.findAllProviders();
                    if (providers.size() > 1 && providerId != providers.get(0).getID()) {
                        for(int i = 1, s = providers.size(); i < s; ++i) {
                            final OpenIdProvider currentProvider = providers.get(i);
                            if (currentProvider.getID() == providerId) {
                                final OpenIdProvider previousProvider = providers.get(i-1);
                                final int order = currentProvider.getOrdering();

                                currentProvider.setOrdering(previousProvider.getOrdering());
                                previousProvider.setOrdering(order);

                                currentProvider.save();
                                previousProvider.save();
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    log.warn("Unable to modify Providers", e);
                }

                return getProvidersResponse();
            }
        });
    }

    @POST
    @Path("/moveDown/{providerId}")
    public Response moveProviderDown(@PathParam("providerId") final int providerId) {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<Response>() {
            @Override
            public Response get() {
                try {
                    final List<OpenIdProvider> providers = openIdDao.findAllProviders();
                    if (providers.size() > 1 && providerId != providers.get(providers.size() - 1).getID()) {
                        for(int i = 0, s = providers.size() - 1; i < s; ++i) {
                            final OpenIdProvider currentProvider = providers.get(i);
                            if (currentProvider.getID() == providerId) {
                                final OpenIdProvider nextProvider = providers.get(i+1);
                                final int order = currentProvider.getOrdering();

                                currentProvider.setOrdering(nextProvider.getOrdering());
                                nextProvider.setOrdering(order);

                                currentProvider.save();
                                nextProvider.save();
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    log.warn("Unable to modify Providers", e);
                }
                return getProvidersResponse();
            }
        });
    }
}

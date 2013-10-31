package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.BasicProviderResponse;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderResponse;
import com.pawelniewiadomski.jira.openid.authentication.servlet.ConfigurationServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

@Path("openIdProviders")
public class OpenIdProvidersResource {

    @Autowired
    OpenIdDao openIdDao;

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login")
    public Response getLogin() {
        try {
            final List<OpenIdProvider> allProviders = sortProvidersByOrder(openIdDao.findAllEnabledProviders());

            final List<BasicProviderResponse> loginProviders = Lists.newArrayList(Iterables.transform(ConfigurationServlet.getOrderedListOfProviders(allProviders),
                    new Function<OpenIdProvider, BasicProviderResponse>() {
                        @Override
                        public BasicProviderResponse apply(@Nullable final OpenIdProvider input) {
                            return new BasicProviderResponse(input.getID(), input.getName());
                        }
                    }));

            return Response.ok(loginProviders).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ImmutableList<OpenIdProvider> sortProvidersByOrder(Iterable<OpenIdProvider> providers) throws SQLException {
        return Ordering.from(new Comparator<OpenIdProvider>() {
            @Override
            public int compare(final OpenIdProvider o1, final OpenIdProvider o2) {
                return o1.getOrder() - o2.getOrder();
            }
        }).immutableSortedCopy(providers);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOpenIdProviders() {
        try {
            final List<OpenIdProvider> providers = sortProvidersByOrder(openIdDao.findAllProviders());
            return Response.ok(Lists.newArrayList(Iterables.transform(ConfigurationServlet.getOrderedListOfProviders(providers),
                    new Function<OpenIdProvider, BasicProviderResponse>() {
                        @Override
                        public BasicProviderResponse apply(@Nullable final OpenIdProvider input) {
                            return new ProviderResponse(input.getID(), input.getName(),
                                    input.isEnabled(), input.getOrder(), input.isInternal(), input.getAllowedDomains());
                        }
                    }))).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CacheControl never() {
        final CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return cc;
    }

}

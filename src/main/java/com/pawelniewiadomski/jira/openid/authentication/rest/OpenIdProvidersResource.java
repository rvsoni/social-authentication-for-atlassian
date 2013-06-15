package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.servlet.ConfigurationServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("openIdProviders")
@AnonymousAllowed
public class OpenIdProvidersResource {

    @Autowired
    OpenIdDao openIdDao;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOpenIdProviders() {
        try {
            final List<OpenIdProvider> providers = openIdDao.findAllEnabledProviders();
            return Response.ok(Lists.newArrayList(Iterables.transform(ConfigurationServlet.getOrderedListOfProviders(providers),
                    new Function<OpenIdProvider, OpenIdProviderModel>() {
                        @Override
                        public OpenIdProviderModel apply(@Nullable final OpenIdProvider input) {
                            return new OpenIdProviderModel(input.getID(), input.getName());
                        }
                    }))).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

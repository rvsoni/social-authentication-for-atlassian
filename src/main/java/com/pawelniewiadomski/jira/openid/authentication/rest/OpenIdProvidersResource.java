package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.BasicProviderBean;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("openIdProviders")
@Produces({MediaType.APPLICATION_JSON})
public class OpenIdProvidersResource extends OpenIdResource {

    @Autowired
    OpenIdDao openIdDao;

    @GET
    @AnonymousAllowed
    @Path("/login")
    public Response getLogin() {
        try {
            final List<BasicProviderBean> loginProviders = Lists.newArrayList(
                    Iterables.transform(openIdDao.findAllEnabledProviders(),
                    new Function<OpenIdProvider, BasicProviderBean>() {
                        @Override
                        public BasicProviderBean apply(@Nullable final OpenIdProvider input) {
                            return new BasicProviderBean(input.getID(), input.getName(), input.getProviderType());
                        }
                    }));

            return Response.ok(loginProviders).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

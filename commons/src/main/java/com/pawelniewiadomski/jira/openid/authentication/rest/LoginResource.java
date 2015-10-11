package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.BasicProviderBean;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("openIdProviders")
@Produces({MediaType.APPLICATION_JSON})
public class LoginResource extends OpenIdResource {

    final OpenIdDao openIdDao;

    public LoginResource(OpenIdDao openIdDao, UserManager userManager) {
        super(userManager);
        this.openIdDao = openIdDao;
    }

    @GET
    @AnonymousAllowed
    @Path("/login")
    public Response getLogin() {
        try {
            final List<BasicProviderBean> loginProviders = Lists.newArrayList(
                    Iterables.transform(openIdDao.findAllEnabledProviders(),
                            new Function<OpenIdProvider, BasicProviderBean>() {
                                @Override
                                public BasicProviderBean apply(@Nullable OpenIdProvider input) {
                                    return new BasicProviderBean(input.getID(), input.getName(), input.getProviderType());
                                }
                            }));

            return Response.ok(loginProviders).cacheControl(never()).build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

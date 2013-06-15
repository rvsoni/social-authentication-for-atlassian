package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

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

@Path("openIdProviders")
public class OpenIdProvidersResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response getOpenIdProviders() {
        return Response.ok().build();
    }

}

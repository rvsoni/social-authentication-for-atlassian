package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("settings")
@Produces({MediaType.APPLICATION_JSON})
public class SettingsResource extends OpenIdResource {
    @Autowired
    GlobalSettings globalSettings;

    @PUT
    public Response setSettings(final Map<String, Object> params) {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<Response>() {
            @Override
            public Response get() {
                final Object creatingUsers = params.get("creatingUsers");
                if (creatingUsers != null) {
                    globalSettings.setCreatingUsers(Boolean.valueOf(creatingUsers.toString()));
                }
                return getSettings();
            }
        });
    }

    @GET
    public Response getSettings() {
        return permissionDeniedIfNotAdmin().getOrElse(new Supplier<Response>() {
            @Override
            public Response get() {
                return Response.ok(ImmutableMap.of("creatingUsers", globalSettings.isCreatingUsers())).build();
            }
        });
    }
}

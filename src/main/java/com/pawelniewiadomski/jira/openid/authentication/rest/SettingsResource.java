package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
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
    final GlobalSettings globalSettings;

    public SettingsResource(GlobalSettings globalSettings, UserManager userManager) {
        super(userManager);
        this.globalSettings = globalSettings;
    }

    @PUT
    public Response setSettings(final Map<String, Object> params) {
        return permissionDeniedIfNotAdmin().orElseGet(() -> {
            final Object creatingUsers = params.get("creatingUsers");
            if (creatingUsers != null) {
                globalSettings.setCreatingUsers(Boolean.valueOf(creatingUsers.toString()));
            }
            return getSettings();
        });
    }

    @GET
    public Response getSettings() {
        return permissionDeniedIfNotAdmin().orElseGet(() -> Response.ok(ImmutableMap.of("creatingUsers", globalSettings.isCreatingUsers())).build());
    }
}

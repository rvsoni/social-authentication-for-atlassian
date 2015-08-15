package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class OpenIdResource {

    final UserManager userManager;

    public OpenIdResource(UserManager userManager) {
        this.userManager = userManager;
    }

    protected Optional<Response> permissionDeniedIfNotAdmin() {
        if (userManager.isAdmin(userManager.getRemoteUsername())) {
            return Optional.empty();
        }
        return Optional.of(Response.status(Response.Status.FORBIDDEN).build());
    }

    public static CacheControl never() {
        final CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return cc;
    }

}

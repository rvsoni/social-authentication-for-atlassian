package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.fugue.Option;
import com.atlassian.sal.api.user.UserManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

@Service
@AllArgsConstructor
public class OpenIdResource {

    final UserManager userManager;

    protected Option<Response> permissionDeniedIfNotAdmin() {
        if (userManager.isAdmin(userManager.getRemoteUsername())) {
            return Option.none();
        }
        return Option.some(Response.status(Response.Status.FORBIDDEN).build());
    }

    public static CacheControl never() {
        final CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return cc;
    }

}

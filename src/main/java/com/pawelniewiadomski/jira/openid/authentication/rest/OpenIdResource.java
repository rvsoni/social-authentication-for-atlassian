package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.atlassian.fugue.Option;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

public class OpenIdResource {

    @Autowired
    JiraAuthenticationContext authenticationContext;

    @Autowired
    PermissionManager permissionManager;

    protected Option<Response> permissionDeniedIfNotAdmin() {
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser())) {
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

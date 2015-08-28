package com.pawelniewiadomski.jira.openid.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import static com.pawelniewiadomski.jira.openid.authentication.BaseUrlHelper.getBaseUrl;

public class OpenIdConnectReturnToHelper
{
    public static String getReturnTo(OpenIdProvider provider, final HttpServletRequest request) {
        return UriBuilder.fromUri(getBaseUrl(request)).path("/openid/oauth2-callback")
                .path(provider.getCallbackId()).build().toString();
    }
}

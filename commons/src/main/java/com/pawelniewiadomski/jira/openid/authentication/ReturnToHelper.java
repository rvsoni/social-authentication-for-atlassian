package com.pawelniewiadomski.jira.openid.authentication;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import static com.pawelniewiadomski.jira.openid.authentication.BaseUrlHelper.getBaseUrl;

@Component
public class ReturnToHelper
{
    @Autowired
    private PluginKey pluginKey;

    public String getReturnTo(OpenIdProvider provider, final HttpServletRequest request) {
        return UriBuilder.fromUri(getBaseUrl(request)).path(pluginKey.getCallbackPath())
                .path(provider.getCallbackId()).build().toString();
    }
}

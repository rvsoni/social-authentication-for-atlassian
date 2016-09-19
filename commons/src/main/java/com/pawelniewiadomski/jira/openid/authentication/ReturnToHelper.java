package com.pawelniewiadomski.jira.openid.authentication;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.BaseUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

@Component
public class ReturnToHelper
{
    @Autowired
    private PluginKey pluginKey;

    @Autowired
    private BaseUrlService baseUrlService;

    public String getReturnTo(OpenIdProvider provider, final HttpServletRequest request) {
        return UriBuilder.fromUri(baseUrlService.getBaseUrl()).path(pluginKey.getCallbackPath())
                .path(provider.getCallbackId()).build().toString();
    }
}

package com.pawelniewiadomski.jira.openid.authentication.services;


import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.OAuth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Handling OpenID Connect authentications.
 */
public class OAuthAuthenticationHandler implements AuthenticationHandler
{
    final TemplateHelper templateHelper;

    final ProviderTypeFactory providerTypeFactory;

    public OAuthAuthenticationHandler(TemplateHelper templateHelper, ProviderTypeFactory providerTypeFactory) {
        this.templateHelper = templateHelper;
        this.providerTypeFactory = providerTypeFactory;
    }

    @Override
    public boolean doAuthenticationDance(OpenIdProvider provider, HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String state = UUID.randomUUID().toString();

        request.getSession().setAttribute(AuthenticationService.STATE_IN_SESSION, state);

        final ProviderType providerType = providerTypeFactory.getProviderTypeById(provider.getProviderType());

        final OAuthClientRequest oauthRequest = ((OAuth2ProviderType) providerType).createOAuthRequest(provider, state, request);

        response.sendRedirect(oauthRequest.getLocationUri());

        return true;
    }
}

package com.pawelniewiadomski.jira.openid.authentication.services;


import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.OAuth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import lombok.AllArgsConstructor;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Handling OpenID Connect authentications.
 */
@Service
public class OAuthAuthenticationHandler implements AuthenticationHandler
{
    @Autowired
    protected TemplateHelper templateHelper;

    @Autowired
    protected ProviderTypeFactory providerTypeFactory;

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

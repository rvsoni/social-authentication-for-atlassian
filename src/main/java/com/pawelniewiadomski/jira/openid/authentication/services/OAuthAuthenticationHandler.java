package com.pawelniewiadomski.jira.openid.authentication.services;


import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.AbstractOAuth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.OAuth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;

/**
 * Handling OpenID Connect authentications.
 */
@Service
public class OAuthAuthenticationHandler implements AuthenticationHandler
{
	@Autowired
    TemplateHelper templateHelper;

    @Autowired
    ProviderTypeFactory providerTypeFactory;

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

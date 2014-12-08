package com.pawelniewiadomski.jira.openid.authentication.services;


import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import org.apache.log4j.Logger;
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
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @Override
    public boolean doAuthenticationDance(OpenIdProvider provider, HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String state = UUID.randomUUID().toString();

        request.getSession().setAttribute(AuthenticationService.STATE_IN_SESSION, state);

        final OpenIdDiscoveryDocumentProvider.OpenIdDiscoveryDocument discoveryDocument = notNull("OpenId Discovery Document must not be null",
                discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()));

        final OAuthClientRequest oauthRequest = OAuthClientRequest
                .authorizationLocation(discoveryDocument.getAuthorizationUrl())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("openid email profile")
                .setParameter("prompt", "select_account")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();

        response.sendRedirect(oauthRequest.getLocationUri());

        return true;
    }
}

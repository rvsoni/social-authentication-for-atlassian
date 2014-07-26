package com.pawelniewiadomski.jira.openid.authentication.services;

import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.pawelniewiadomski.jira.openid.authentication.openid.OpenIdDiscoveryResponse;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.springframework.stereotype.Service;

@Service
public class OpenIdDiscoveryDocumentProvider
{
    @JsonAutoDetect
    public class OpenIdDiscoveryDocument
    {
        private final String tokenUrl;
        private final String authorizationUrl;
        private final String userinfoUrl;

        public OpenIdDiscoveryDocument(String tokenUrl, String authorizationUrl, String userinfoUrl)
        {
            this.tokenUrl = tokenUrl;
            this.authorizationUrl = authorizationUrl;
            this.userinfoUrl = userinfoUrl;
        }

        public String getTokenUrl()
        {
            return tokenUrl;
        }

        public String getAuthorizationUrl()
        {
            return authorizationUrl;
        }

        public String getUserinfoUrl()
        {
            return userinfoUrl;
        }
    }

    private final Cache<String, OpenIdDiscoveryDocument> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<String, OpenIdDiscoveryDocument>()
            {
                @Override
                public OpenIdDiscoveryDocument load(@Nonnull String key) throws Exception
                {
                    final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
                    final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(key + "/.well-known/openid-configuration")
                            .buildQueryMessage();

                    OpenIdDiscoveryResponse response = oAuthClient.resource(oAuthRequest, OAuth.HttpMethod.GET, OpenIdDiscoveryResponse.class);
                    return new OpenIdDiscoveryDocument(response.getParam("token_endpoint"), response.getParam("authorization_endpoint"),
                            response.getParam("userinfo_endpoint"));
                }
            });

    @Nullable
    public OpenIdDiscoveryDocument getDiscoveryDocument(@Nonnull String providersUrl) throws ExecutionException
    {
        return cache.get(providersUrl);
    }
}

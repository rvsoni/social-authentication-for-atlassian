package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;

public class FacebookProviderType extends AbstractOAuth2ProviderType {

    public FacebookProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getAuthorizationUrl() {
        return OAuthProviderType.FACEBOOK.getAuthzEndpoint();
    }

    @Nonnull
    @Override
    public String getCallbackId() {
        return "facebook";
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.FACEBOOK_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.facebook");
    }

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        return OAuthClientRequest
                .authorizationLocation(OAuthProviderType.FACEBOOK.getAuthzEndpoint())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("public_profile email")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Override
    public Either<Pair<String, String>, String> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, @Nonnull HttpServletRequest request) throws Exception {
        final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(OAuthProviderType.FACEBOOK.getTokenEndpoint())
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(provider.getClientId())
                .setClientSecret(provider.getClientSecret())
                .setRedirectURI(getReturnTo(provider, request))
                .setCode(authorizationCode)
                .buildQueryMessage();

        final GitHubTokenResponse token = oAuthClient.accessToken(oAuthRequest, GitHubTokenResponse.class);
        final String accessToken = token.getAccessToken();

        final OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://graph.facebook.com/me")
                .setAccessToken(accessToken)
                .buildQueryMessage();

        final OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        final Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());

        return Either.left(Pair.pair((String) userInfo.get("name"), (String) userInfo.get("email")));
    }
}

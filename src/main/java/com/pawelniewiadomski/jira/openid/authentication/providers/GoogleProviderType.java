package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.jira.util.lang.Pair;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.openid.OpenIdConnectResponse;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

public class GoogleProviderType extends AbstractOAuth2ProviderType {

    public GoogleProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.GOOGLE_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.google");
    }

    @Nonnull
    @Override
    public String getAuthorizationUrl() {
        return OAuthProviderType.GOOGLE.getAuthzEndpoint();
    }

    @Nonnull
    @Override
    public String getCallbackId() {
        return "google";
    }

    @Nullable
    @Override
    public String getCreatedProviderName() {
        return "Google";
    }

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        return OAuthClientRequest
                .authorizationLocation(OAuthProviderType.GOOGLE.getAuthzEndpoint())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("openid email profile")
                .setParameter("prompt", "select_account")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Override
    public Pair<String, String> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, @Nonnull HttpServletRequest request) throws Exception {
        final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(OAuthProviderType.GOOGLE.getTokenEndpoint())
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(provider.getClientId())
                .setClientSecret(provider.getClientSecret())
                .setRedirectURI(getReturnTo(provider, request))
                .setCode(authorizationCode)
                .buildBodyMessage();

        final OpenIdConnectResponse token = oAuthClient.accessToken(oAuthRequest, OpenIdConnectResponse.class);
        final String accessToken = token.getAccessToken();
        final String email = token.getIdToken().getClaimsSet().getCustomField("email", String.class);

        final OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://www.googleapis.com/plus/v1/people/me/openIdConnect")
                .setAccessToken(accessToken)
                .buildQueryMessage();

        final OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        final Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());
        final String username = defaultIfEmpty((String) userInfo.get("name"), email);

        return Pair.of(username, email);
    }
}

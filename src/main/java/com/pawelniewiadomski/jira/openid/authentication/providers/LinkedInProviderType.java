package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.jira.util.lang.Pair;
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
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;

public class LinkedInProviderType extends AbstractOAuth2ProviderType {

    public LinkedInProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getAuthorizationUrl() {
        return OAuthProviderType.LINKEDIN.getAuthzEndpoint();
    }

    @Nonnull
    @Override
    public String getCallbackId() {
        return "linkedin";
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.LINKED_IN_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.linkedin");
    }

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        return OAuthClientRequest
                .authorizationLocation(OAuthProviderType.LINKEDIN.getAuthzEndpoint())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("r_basicprofile r_emailaddress")
                .setParameter("prompt", "select_account")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Override
    public Pair<String, String> getUsernameAndEmail(@Nonnull String code, @Nonnull OpenIdProvider provider, HttpServletRequest request) throws Exception {
        final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(OAuthProviderType.LINKEDIN.getTokenEndpoint())
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(provider.getClientId())
                .setClientSecret(provider.getClientSecret())
                .setRedirectURI(getReturnTo(provider, request))
                .setCode(code)
                .buildBodyMessage();

        final GitHubTokenResponse token = oAuthClient.accessToken(oAuthRequest, GitHubTokenResponse.class);
        final String accessToken = token.getAccessToken();

        final OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://api.linkedin.com/v1/people/~:(id,email-address,first-name,last-name)?format=json")
                .setAccessToken(accessToken)
                .buildQueryMessage();

        final OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        final Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());

        return Pair.of(userInfo.get("first-name") + " " + userInfo.get("last-name"), (String) userInfo.get("email"));
    }
}
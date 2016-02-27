package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.ReturnToHelper;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class LinkedInProviderType extends AbstractOAuth2ProviderType {

    private final ReturnToHelper returnToHelper;

    public LinkedInProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao, ReturnToHelper returnToHelper) {
        super(i18nResolver, openIdDao);
        this.returnToHelper = returnToHelper;
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
                .setRedirectURI(returnToHelper.getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Override
    public Either<Pair<String, String>, Error> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, HttpServletRequest request) throws Exception {
        final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(OAuthProviderType.LINKEDIN.getTokenEndpoint())
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(provider.getClientId())
                .setClientSecret(provider.getClientSecret())
                .setRedirectURI(returnToHelper.getReturnTo(provider, request))
                .setCode(authorizationCode)
                .buildQueryMessage();

        final OAuthJSONAccessTokenResponse token = oAuthClient.accessToken(oAuthRequest, OAuthJSONAccessTokenResponse.class);
        final String accessToken = token.getAccessToken();

        final OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://api.linkedin.com/v1/people/~:(id,email-address,first-name,last-name)?format=json")
                .setAccessToken(accessToken)
                .buildHeaderMessage();

        final OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        final Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());

        return Either.left(Pair.pair(userInfo.get("firstName") + " " + userInfo.get("lastName"), (String) userInfo.get("emailAddress")));
    }
}
package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.openid.OpenIdConnectResponse;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class DiscoverablyOauth2ProviderType extends AbstractProviderType implements OAuth2ProviderType {
    private final OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    public DiscoverablyOauth2ProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao, OpenIdDiscoveryDocumentProvider discoveryDocumentProvider) {
        super(i18nResolver, openIdDao);
        this.discoveryDocumentProvider = discoveryDocumentProvider;
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.OAUTH2_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.oauth2");
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateCreate(ProviderBean providerBean) {
        return validateUpdate(null, providerBean);
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateUpdate(OpenIdProvider provider, ProviderBean providerBean) {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();

        validateName(provider, providerBean, errors);

        if (isEmpty(providerBean.getEndpointUrl())) {
            errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
        } else {
            try {
                discoveryDocumentProvider.getDiscoveryDocument(providerBean.getEndpointUrl());
            } catch (Exception e) {
                errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.discovery.missing", providerBean.getEndpointUrl()));
            }
        }

        if (isEmpty(providerBean.getClientId())) {
            errors.addError("clientId", i18nResolver.getText("configuration.clientId.empty"));
        }
        if (isEmpty(providerBean.getClientSecret())) {
            errors.addError("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
        }
        if (isEmpty(providerBean.getCallbackId())) {
            errors.addErrorMessage(i18nResolver.getText("configuration.callbackId.empty"));
        }

        if (errors.hasAnyErrors()) {
            return Either.left(ErrorCollection.of(errors));
        } else {
            return Either.right(MapBuilder.<String, Object>newBuilder()
                    .add(OpenIdProvider.NAME, providerBean.getName())
                    .add(OpenIdProvider.ENDPOINT_URL, providerBean.getEndpointUrl())
                    .add(OpenIdProvider.PROVIDER_TYPE, "oauth2")
                    .add(OpenIdProvider.CLIENT_ID, providerBean.getClientId())
                    .add(OpenIdProvider.CLIENT_SECRET, providerBean.getClientSecret())
                    .add(OpenIdProvider.CALLBACK_ID, providerBean.getCallbackId())
                    .add(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains()).toMap());
        }
    }

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        final OpenIdDiscoveryDocumentProvider.OpenIdDiscoveryDocument discoveryDocument = notNull("OpenId Discovery Document must not be null",
                discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()));

        return OAuthClientRequest
                .authorizationLocation(discoveryDocument.getAuthorizationUrl())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("openid email profile")
                .setParameter("prompt", "select_account")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Nonnull
    public String getTokenUrl(@Nonnull OpenIdProvider provider) throws Exception {
        return discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()).getTokenUrl();
    }

    @Nonnull
    public String getUserInfoUrl(@Nonnull OpenIdProvider provider) throws Exception {
        return discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()).getUserinfoUrl();
    }

    @Override
    public Pair<String, String> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, @Nonnull HttpServletRequest request) throws Exception {
        final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(getTokenUrl(provider))
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(provider.getClientId())
                .setClientSecret(provider.getClientSecret())
                .setRedirectURI(getReturnTo(provider, request))
                .setCode(authorizationCode)
                .buildBodyMessage();

        final OpenIdConnectResponse token = oAuthClient.accessToken(oAuthRequest, OpenIdConnectResponse.class);
        final String accessToken = token.getAccessToken();
        final String email = token.getIdToken().getClaimsSet().getCustomField("email", String.class);
        String username = email;

        final String userInfoUrl = getUserInfoUrl(provider);
        if (isNotEmpty(userInfoUrl))
        {
            OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(userInfoUrl)
                    .setAccessToken(accessToken)
                    .buildQueryMessage();

            OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());
            username = defaultIfEmpty((String) userInfo.get("name"), email);
        }
        return Pair.of(username, email);
    }
}

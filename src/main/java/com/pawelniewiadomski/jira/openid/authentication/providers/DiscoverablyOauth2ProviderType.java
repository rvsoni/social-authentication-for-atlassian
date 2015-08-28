package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
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
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;
import static org.apache.commons.lang.StringUtils.*;

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
    public Either<Errors, Map<String, Object>> validateCreate(ProviderBean providerBean) {
        return validateUpdate(null, providerBean);
    }

    @Nullable
    protected String getSslRelatedError(@Nonnull Exception e) {
        for(Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SSLException) {
                return t.getMessage();
            }
        }
        return null;
    }

    private boolean isCertificateKeyTooLong(Exception e) {
        for(Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SSLException) {
                return t.getMessage().contains("Could not generate DH keypair");
            }
        }
        return false;
    }

    @Override
    public Either<Errors, Map<String, Object>> validateUpdate(OpenIdProvider provider, ProviderBean providerBean) {
        Errors errors = new Errors();

        validateName(provider, providerBean, errors);

        if (isEmpty(providerBean.getEndpointUrl())) {
            errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
        } else {
            try {
                discoveryDocumentProvider.getDiscoveryDocument(providerBean.getEndpointUrl());
            } catch (Exception e) {
                String sslError = getSslRelatedError(e);
                if (isCertificateKeyTooLong(e)) {
                    errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.ssl.key.too.long"));
                } else if (sslError != null) {
                    errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.ssl.error", sslError));
                } else {
                    errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.discovery.missing", providerBean.getEndpointUrl()));
                }
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
            return Either.left(errors);
        } else {
            return Either.right(ImmutableMap.<String, Object>builder()
                    .put(OpenIdProvider.NAME, providerBean.getName())
                    .put(OpenIdProvider.ENDPOINT_URL, providerBean.getEndpointUrl())
                    .put(OpenIdProvider.PROVIDER_TYPE, "oauth2")
                    .put(OpenIdProvider.CLIENT_ID, providerBean.getClientId())
                    .put(OpenIdProvider.CLIENT_SECRET, providerBean.getClientSecret())
                    .put(OpenIdProvider.CALLBACK_ID, providerBean.getCallbackId())
                    .put(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains()).build());
        }
    }

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        final OpenIdDiscoveryDocumentProvider.OpenIdDiscoveryDocument discoveryDocument = checkNotNull(
                discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()),
                "OpenId Discovery Document must not be null");

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
    public Either<Pair<String, String>, String> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, @Nonnull HttpServletRequest request) throws Exception {
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
                    .buildHeaderMessage();

            OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());
            username = defaultIfEmpty((String) userInfo.get("name"), email);
        }
        return Either.left(Pair.pair(username, email));
    }
}

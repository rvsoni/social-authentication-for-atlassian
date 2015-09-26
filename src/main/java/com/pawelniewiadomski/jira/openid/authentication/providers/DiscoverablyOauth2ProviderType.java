package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.atlassian.sal.api.message.I18nResolver;
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
import org.apache.oltu.oauth2.jwt.ClaimsSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
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

    @Nullable
    protected String getSslRelatedError(@Nonnull Exception e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SSLException) {
                return t.getMessage();
            }
        }
        return null;
    }

    private boolean isCertificateKeyTooLong(Exception e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SSLException) {
                return t.getMessage().contains("Could not generate DH keypair");
            }
        }
        return false;
    }

    @Override
    public Either<Errors, OpenIdProvider> createOrUpdate(OpenIdProvider provider, ProviderBean providerBean) {
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
        } else if (provider == null) {
            Map<String, Object> map = new HashMap<>();
            map.put(OpenIdProvider.NAME, providerBean.getName());
            map.put(OpenIdProvider.ENDPOINT_URL, providerBean.getEndpointUrl());
            map.put(OpenIdProvider.PROVIDER_TYPE, "oauth2");
            map.put(OpenIdProvider.CLIENT_ID, providerBean.getClientId());
            map.put(OpenIdProvider.CLIENT_SECRET, providerBean.getClientSecret());
            map.put(OpenIdProvider.CALLBACK_ID, providerBean.getCallbackId());
            map.put(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains());
            try {
                return Either.right(openIdDao.createProvider(map));
            } catch (SQLException e) {
                return Either.left(new Errors().addErrorMessage("Error when saving the provider: " + e.getMessage()));
            }
        } else {
            provider.setName(providerBean.getName());
            provider.setEndpointUrl(providerBean.getEndpointUrl());
            provider.setProviderType("oauth2");
            provider.setProviderType(getId());
            provider.setClientId(providerBean.getClientId());
            provider.setClientSecret(providerBean.getClientSecret());
            provider.setCallbackId(providerBean.getCallbackId());
            provider.setAllowedDomains(providerBean.getAllowedDomains());
            provider.save();
            return Either.right(provider);
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
        ClaimsSet claimsSet = token.getIdToken().getClaimsSet();
        String email = claimsSet.getCustomField("email", String.class);
        email = defaultIfEmpty(email, claimsSet.getCustomField("upn", String.class));
        String username = defaultIfEmpty(claimsSet.getCustomField("name", String.class), email);

        final String userInfoUrl = getUserInfoUrl(provider);
        if ((email == null || username == null) && isNotEmpty(userInfoUrl)) {
            OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(userInfoUrl)
                    .setAccessToken(accessToken)
                    .buildHeaderMessage();

            OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());
            // as a last resort try upn from user info (in case of Azure that's email)
            email = defaultIfEmpty(email, (String) userInfo.get("upn"));
            username = defaultIfEmpty((String) userInfo.get("name"), email);
        }
        return Either.left(Pair.pair(username, email));
    }
}

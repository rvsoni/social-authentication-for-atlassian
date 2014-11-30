package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Oauth2ProviderType extends AbstractProviderType {
    private final OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    public Oauth2ProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao, OpenIdDiscoveryDocumentProvider discoveryDocumentProvider) {
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
}

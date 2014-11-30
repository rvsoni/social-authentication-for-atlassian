package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class GoogleProviderType implements ProviderType {
    private final I18nResolver i18nResolver;

    public GoogleProviderType(I18nResolver i18nResolver) {
        this.i18nResolver = i18nResolver;
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

    @Override
    public boolean isSkipClientInfo() {
        return false;
    }

    @Override
    public boolean isSkipCallback() {
        return true;
    }

    @Override
    public boolean isSkipUrl() {
        return true;
    }

    @Nullable
    @Override
    public String getCreatedProviderName() {
        return "Google";
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateCreate(ProviderBean providerBean) {
        return validateUpdate(null, providerBean);
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateUpdate(OpenIdProvider provider, ProviderBean providerBean) {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();

        if (isEmpty(providerBean.getClientId())) {
            errors.addError("clientId", i18nResolver.getText("configuration.clientId.empty"));
        }
        if (isEmpty(providerBean.getClientSecret())) {
            errors.addError("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
        }

        if (errors.hasAnyErrors()) {
            return Either.left(ErrorCollection.of(errors));
        } else {
            return Either.right(MapBuilder.<String, Object>newBuilder()
                    .add(OpenIdProvider.NAME, "Google")
                    .add(OpenIdProvider.ENDPOINT_URL, "https://accounts.google.com")
                    .add(OpenIdProvider.CALLBACK_ID, "google")
                    .add(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains())
                    .add(OpenIdProvider.PROVIDER_TYPE, "google")
                    .add(OpenIdProvider.CLIENT_ID, providerBean.getClientId())
                    .add(OpenIdProvider.CLIENT_SECRET, providerBean.getClientSecret())
                    .toMap());
        }
    }
}

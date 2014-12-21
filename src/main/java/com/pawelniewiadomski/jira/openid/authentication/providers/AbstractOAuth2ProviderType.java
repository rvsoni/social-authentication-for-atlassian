package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import org.apache.oltu.oauth2.common.OAuthProviderType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class AbstractOAuth2ProviderType extends AbstractProviderType implements OAuth2ProviderType {
    public AbstractOAuth2ProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Override
    public boolean isSkipClientInfo() {
        return false;
    }

    @Override
    public boolean isSkipCallback() {
        return false;
    }

    @Override
    public boolean isSkipUrl() {
        return true;
    }

    @Nonnull
    public abstract String getAuthorizationUrl();

    @Nonnull
    public abstract String getCallbackId();

    @Nullable
    @Override
    public String getCreatedProviderName() {
        return getName();
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateCreate(ProviderBean providerBean) {
        return validateUpdate(null, providerBean);
    }

    @Override
    public Either<ErrorCollection, Map<String, Object>> validateUpdate(OpenIdProvider provider, ProviderBean providerBean) {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();

        validateName(provider, getCreatedProviderName(), errors);

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
                    .add(OpenIdProvider.NAME, getCreatedProviderName())
                    .add(OpenIdProvider.ENDPOINT_URL, getAuthorizationUrl())
                    .add(OpenIdProvider.CALLBACK_ID, getCallbackId()) // changing the id will break all existing urls
                    .add(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains())
                    .add(OpenIdProvider.PROVIDER_TYPE, getId())
                    .add(OpenIdProvider.CLIENT_ID, providerBean.getClientId())
                    .add(OpenIdProvider.CLIENT_SECRET, providerBean.getClientSecret())
                    .toMap());
        }
    }
}

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OpenIdProviderType extends AbstractProviderType {

    public OpenIdProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.OPENID_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.openid1");
    }

    @Override
    public boolean isSkipClientInfo() {
        return true;
    }

    @Override
    public boolean isSkipCallback() {
        return true;
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
        }

        if (isEmpty(providerBean.getExtensionNamespace())) {
            errors.addError("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
        }

        if (errors.hasAnyErrors()) {
            return Either.left(ErrorCollection.of(errors));
        } else {
            return Either.right(MapBuilder.<String, Object>newBuilder()
                    .add(OpenIdProvider.NAME, providerBean.getName())
                    .add(OpenIdProvider.ENDPOINT_URL, providerBean.getEndpointUrl())
                    .add(OpenIdProvider.PROVIDER_TYPE, "openid1")
                    .add(OpenIdProvider.EXTENSION_NAMESPACE, providerBean.getExtensionNamespace())
                    .add(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains()).toMap());
        }
    }

}

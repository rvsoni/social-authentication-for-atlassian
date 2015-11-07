package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.prechomp;

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
    public Either<Errors, OpenIdProvider> createOrUpdate(@Nullable OpenIdProvider provider, @Nonnull ProviderBean providerBean) {
        Errors errors = new Errors();

        validateName(provider, providerBean, errors);

        if (isEmpty(providerBean.getEndpointUrl())) {
            errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
        }

        if (isEmpty(providerBean.getExtensionNamespace())) {
            errors.addError("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
        }

        if (errors.hasAnyErrors()) {
            return Either.left(errors);
        } else if (provider == null) {
            Map<String, Object> map = new HashMap<>();
            map.put(OpenIdProvider.NAME, providerBean.getName());
            map.put(OpenIdProvider.ENDPOINT_URL, providerBean.getEndpointUrl());
            map.put(OpenIdProvider.PROVIDER_TYPE, "openid1");
            map.put(OpenIdProvider.EXTENSION_NAMESPACE, providerBean.getExtensionNamespace());
            map.put(OpenIdProvider.ALLOWED_DOMAINS, providerBean.getAllowedDomains());
            try {
                return Either.right(openIdDao.createProvider(map));
            } catch (SQLException e) {
                return Either.left(new Errors().addErrorMessage("Error when saving the provider: " + e.getMessage()));
            }
        } else {
            provider.setName(providerBean.getName());
            provider.setEndpointUrl(providerBean.getEndpointUrl());
            provider.setProviderType("openid1");
            provider.setExtensionNamespace(providerBean.getExtensionNamespace());
            provider.setAllowedDomains(providerBean.getAllowedDomains());
            provider.save();
            return Either.right(provider);
        }
    }

}

package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.GoogleProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.Oauth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.OpenIdProviderType;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;

@Service
public class ProviderTypeFactory {
    @Autowired
    private I18nResolver i18nResolver;

    @Autowired
    private OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @Autowired
    private OpenIdDao openIdDao;

    @Nonnull
    public ProviderType getProviderTypeById(@Nonnull String id) throws IllegalArgumentException {
        final ProviderType providerType = ID_MAP.get().get(id);
        if (providerType != null) {
            return providerType;
        }
        throw new IllegalArgumentException("No such ProviderType with id " + id);
    }

    @Nonnull
    public Map<String, ProviderType> getAllProviderTypes() {
        return ID_MAP.get();
    }

    private final Supplier<Map<String, ProviderType>> ID_MAP = Suppliers.memoize(new Supplier<Map<String, ProviderType>>() {
        @Override
        public Map<String, ProviderType> get() {
            return ImmutableMap.<String, ProviderType>builder()
                    .put(OpenIdProvider.GOOGLE_TYPE, new GoogleProviderType(i18nResolver, openIdDao))
                    .put(OpenIdProvider.OPENID_TYPE, new OpenIdProviderType(i18nResolver, openIdDao))
                    .put(OpenIdProvider.OAUTH2_TYPE, new Oauth2ProviderType(i18nResolver, openIdDao, discoveryDocumentProvider)).build();
        }
    });
}

package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.providers.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProviderTypeFactory {
    private final I18nResolver i18nResolver;

    private final OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    private final OpenIdDao openIdDao;

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
            final ImmutableList<ProviderType> providerTypes = ImmutableList.<ProviderType>of(
                    new GoogleProviderType(i18nResolver, openIdDao),
                    new FacebookProviderType(i18nResolver, openIdDao),
                    new LinkedInProviderType(i18nResolver, openIdDao),
                    new GitHubProviderType(i18nResolver, openIdDao),
                    new VkProviderType(i18nResolver, openIdDao),
                    new OpenIdProviderType(i18nResolver, openIdDao),
                    new DiscoverablyOauth2ProviderType(i18nResolver, openIdDao, discoveryDocumentProvider));

            return ImmutableMap.<String, ProviderType>builder().putAll(Maps.uniqueIndex(providerTypes, new Function<ProviderType, String>() {
                @Override
                public String apply(@Nullable ProviderType abstractProviderType) {
                    return abstractProviderType.getId();
                }
            })).build();
        }
    });
}

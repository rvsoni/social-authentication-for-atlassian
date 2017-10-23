package com.pawelniewiadomski.jira.openid.authentication.services;

import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;

import javax.annotation.Nonnull;
import java.util.Map;

public interface ProviderTypeFactory {
    @Nonnull
    ProviderType getProviderTypeById(@Nonnull String id) throws IllegalArgumentException;

    @Nonnull
    Map<String, ProviderType> getAllProviderTypes();
}

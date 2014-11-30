package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface ProviderType {

    @Nonnull
    String getId();

    @Nonnull
    String getName();

    boolean isSkipClientInfo();

    boolean isSkipCallback();

    boolean isSkipUrl();

    @Nullable
    String getCreatedProviderName();

    @Nonnull
    Either<ErrorCollection, Map<String, Object>> validateCreate(@Nonnull ProviderBean providerBean);

    @Nonnull
    Either<ErrorCollection, Map<String, Object>> validateUpdate(@Nonnull OpenIdProvider provider, @Nonnull ProviderBean providerBean);
}

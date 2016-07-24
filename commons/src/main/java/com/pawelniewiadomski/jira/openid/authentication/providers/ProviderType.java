package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProviderType {

    @Nonnull
    String getId();

    @Nonnull
    String getName();

    boolean isSkipClientInfo();

    boolean isSkipCallback();

    boolean isSkipUrl();

    List<String> getSupportedPrompts();

    @Nullable
    String getCreatedProviderName();

    @Nonnull
    Either<Errors, OpenIdProvider> createOrUpdate(@Nullable OpenIdProvider provider, @Nonnull ProviderBean providerBean);
}

package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.pawelniewiadomski.jira.openid.authentication.Errors;
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
    Either<Errors, Map<String, Object>> validateCreate(@Nonnull ProviderBean providerBean);

    @Nonnull
    Either<Errors, Map<String, Object>> validateUpdate(@Nonnull OpenIdProvider provider, @Nonnull ProviderBean providerBean);
}

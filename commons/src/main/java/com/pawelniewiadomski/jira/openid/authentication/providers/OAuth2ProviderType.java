package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public interface OAuth2ProviderType extends ProviderType {

    OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                          @Nonnull String state,
                                          @Nonnull HttpServletRequest request) throws Exception;

    Either<Pair<String, String>, Error> getUsernameAndEmail(@Nonnull String authorizationCode, @Nonnull OpenIdProvider provider, HttpServletRequest request) throws Exception;

    @Data
    @Builder
    class Error {
        private final String errorMessage;
        private final String payload;
    }
}

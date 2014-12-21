package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.jira.util.lang.Pair;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public interface OAuth2ProviderType extends ProviderType {

    OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                          @Nonnull String state,
                                          @Nonnull HttpServletRequest request) throws Exception;

    Pair<String,String> getUsernameAndEmail(@Nonnull String code, @Nonnull OpenIdProvider provider, HttpServletRequest request) throws Exception;
}

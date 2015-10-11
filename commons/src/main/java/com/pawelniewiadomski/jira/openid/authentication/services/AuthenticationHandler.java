package com.pawelniewiadomski.jira.openid.authentication.services;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthenticationHandler {
    boolean doAuthenticationDance(@Nonnull OpenIdProvider provider, @Nonnull HttpServletRequest request,
                                  @Nonnull HttpServletResponse response) throws Exception;
}


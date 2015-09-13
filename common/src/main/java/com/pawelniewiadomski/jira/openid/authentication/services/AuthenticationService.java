package com.pawelniewiadomski.jira.openid.authentication.services;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthenticationService {
    String RETURN_URL_SESSION = AuthenticationService.class.getName() + ".returnUrl";
    String STATE_IN_SESSION = AuthenticationService.class.getName() + ".state";
    String RETURN_URL_PARAM = "returnUrl";

    void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                            final OpenIdProvider provider, String identity, String email) throws IOException, ServletException;

}

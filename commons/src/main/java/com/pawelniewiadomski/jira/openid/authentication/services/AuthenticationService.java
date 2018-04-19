package com.pawelniewiadomski.jira.openid.authentication.services;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthenticationService {
    String RETURN_URL_SESSION = AuthenticationService.class.getName() + ".returnUrl";
    String STATE_IN_SESSION = AuthenticationService.class.getName() + ".state";
    String PORTAL_ID_SESSION = AuthenticationService.class.getName() + ".portalId";
    String RETURN_URL_PARAM = "returnUrl";
    String PORTAL_ID_PARAM = "portalId";

    void showAuthentication(HttpServletRequest request, HttpServletResponse response,
                            OpenIdProvider provider, ProvidedUserDetails userDetails) throws IOException, ServletException;

}

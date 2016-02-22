package com.pawelniewiadomski.jira.openid.authentication.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.pawelniewiadomski.jira.openid.authentication.BaseUrlHelper.getBaseUrl;
import static com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService.RETURN_URL_SESSION;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;

@Service
public class RedirectionService {
    @Autowired private HomePageService homePageService;

    public void redirectToReturnUrlOrHome(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final String returnUrl = (String) request.getSession().getAttribute(RETURN_URL_SESSION);

        if (isNotBlank(returnUrl)) {
            if (startsWith(returnUrl, "http")) {
                response.sendRedirect(returnUrl);
            } else {
                response.sendRedirect(getBaseUrl(request) + returnUrl);
            }
        } else {
            response.sendRedirect(getBaseUrl(request) + homePageService.getHomePagePath());
        }
    }
}

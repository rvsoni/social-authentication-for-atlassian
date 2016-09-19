package com.pawelniewiadomski.jira.openid.authentication.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService.RETURN_URL_SESSION;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;

@Slf4j
@Service
public class RedirectionService {
    @Autowired
    private HomePageService homePageService;

    @Autowired
    private BaseUrlService baseUrlService;

    public void redirectToReturnUrlOrHome(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String redirectTo = baseUrlService.getBaseUrl() + homePageService.getHomePagePath();

        final String returnUrl = (String) request.getSession().getAttribute(RETURN_URL_SESSION);
        if (isNotBlank(returnUrl)) {
            if (startsWith(returnUrl, "http")) {
                redirectTo = returnUrl;
            } else if (startsWith(returnUrl, "portal")) {
                redirectTo = baseUrlService.getBaseUrl() + "/servicedesk/customer/" + returnUrl;
            } else {
                redirectTo = baseUrlService.getBaseUrl() + returnUrl;
            }
        }

        request.setAttribute("com.atlassian.web.servlet.plugin.request.RedirectInterceptingResponse.sendRedirect", true);
        response.sendRedirect(redirectTo);
    }
}

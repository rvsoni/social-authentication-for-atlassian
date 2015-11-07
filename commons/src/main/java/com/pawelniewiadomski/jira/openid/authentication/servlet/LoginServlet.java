package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.services.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class LoginServlet extends HttpServlet {

    @Autowired protected LicenseProvider licenseProvider;

    @Autowired protected TemplateHelper templateHelper;

    @Autowired protected OAuthAuthenticationHandler oAuthAuthenticationHandler;

    @Autowired protected OpenIdAuthenticationHandler openIdAuthenticationHandler;

    @Autowired
    protected OpenIdDao openIdDao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!licenseProvider.isValidLicense()) {
            templateHelper.render(request, response, "OpenId.Templates.invalidLicense");
            return;
        }

        final String pid = request.getParameter("pid");
        final String returnUrl = request.getParameter(AuthenticationService.RETURN_URL_PARAM);
        if (StringUtils.isNotBlank(returnUrl)) {
            request.getSession().setAttribute(AuthenticationService.RETURN_URL_SESSION, returnUrl);
        }

        final OpenIdProvider provider;
        try {
            provider = openIdDao.findProvider(Integer.valueOf(pid));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            if (provider != null) {
                final AuthenticationHandler authenticationHandler = OpenIdProvider.OPENID_TYPE.equals(provider.getProviderType())
                        ? openIdAuthenticationHandler : oAuthAuthenticationHandler;
                if (authenticationHandler.doAuthenticationDance(provider, request, response)) {
                    return;
                }
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        templateHelper.render(request, response, "OpenId.Templates.error");
    }
}

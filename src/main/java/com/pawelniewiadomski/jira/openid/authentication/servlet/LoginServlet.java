package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationHandler;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.OAuthAuthenticationHandler;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdAuthenticationHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class LoginServlet extends AbstractOpenIdServlet {

    final Logger log = Logger.getLogger(this.getClass());

    final LicenseProvider licenseProvider;

    final TemplateHelper templateHelper;

    final OAuthAuthenticationHandler oAuthAuthenticationHandler;

    final OpenIdAuthenticationHandler openIdAuthenticationHandler;

    public LoginServlet(LicenseProvider licenseProvider, TemplateHelper templateHelper,
                        OAuthAuthenticationHandler oAuthAuthenticationHandler,
                        OpenIdAuthenticationHandler openIdAuthenticationHandler, OpenIdDao openIdDao) {
        super(openIdDao);
        this.licenseProvider = licenseProvider;
        this.templateHelper = templateHelper;
        this.oAuthAuthenticationHandler = oAuthAuthenticationHandler;
        this.openIdAuthenticationHandler = openIdAuthenticationHandler;
    }

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
                final AuthenticationHandler authenticationHandler = OpenIdProvider.OPENID_TYPE.equals(provider.getProviderType()) ? openIdAuthenticationHandler : oAuthAuthenticationHandler;
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

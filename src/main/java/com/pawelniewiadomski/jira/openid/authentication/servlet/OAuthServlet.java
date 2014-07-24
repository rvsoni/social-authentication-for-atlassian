package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.security.GoogleAuthenticationService;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Handling OpenID Connect authentications.
 */
public class OAuthServlet extends AbstractOpenIdServlet
{
    final Logger log = Logger.getLogger(this.getClass());

	@Autowired
    LicenseProvider licenseProvider;

    @Autowired
    TemplateHelper templateHelper;

    @Autowired
    OpenIdDao openIdDao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!licenseProvider.isValidLicense()) {
            templateHelper.render(request, response, "OpenId.Templates.invalidLicense", Collections.<String, Object>emptyMap());
            return;
        }

        final String pid = request.getParameter("pid");
        final String returnUrl = request.getParameter(AuthenticationService.RETURN_URL_PARAMETER);
        if (StringUtils.isNotBlank(returnUrl)) {
            request.getSession().setAttribute(AuthenticationService.RETURN_URL_PARAMETER, returnUrl);
        }

        final OpenIdProvider provider;
        try {
            provider = openIdDao.findProvider(Integer.valueOf(pid));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            if (provider != null) {
                final String state = UUID.randomUUID().toString();

                request.getSession().setAttribute(AuthenticationService.STATE_IN_SESSION, state);

                GoogleAuthenticationService authenticationService = new GoogleAuthenticationService(provider.getClientId(),
                        provider.getClientSecret());

                OAuth2ConnectionFactory<Google> connectionFactory = authenticationService.getConnectionFactory();

                final OAuth2Parameters parameters = new OAuth2Parameters();
                parameters.setScope("openid profile email https://www.googleapis.com/auth/plus.login");
                parameters.setState(state);
                parameters.setRedirectUri(getReturnTo(provider, request));
                parameters.set("prompt", "select_account");

                response.sendRedirect(connectionFactory.getOAuthOperations().buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE,
                        parameters));
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());    }
}

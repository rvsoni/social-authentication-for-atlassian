package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;

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

    @Autowired
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!licenseProvider.isValidLicense()) {
            templateHelper.render(request, response, "OpenId.Templates.invalidLicense", Collections.<String, Object>emptyMap());
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
                final String state = UUID.randomUUID().toString();

                request.getSession().setAttribute(AuthenticationService.STATE_IN_SESSION, state);

                final OpenIdDiscoveryDocumentProvider.OpenIdDiscoveryDocument discoveryDocument = notNull("OpenId Discovery Document must not be null",
                        discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()));

                final OAuthClientRequest oauthRequest = OAuthClientRequest
                        .authorizationLocation(discoveryDocument.getAuthorizationUrl())
                        .setClientId(provider.getClientId())
                        .setResponseType(ResponseType.CODE.toString())
                        .setState(state)
                        .setScope("openid email profile")
                        .setParameter("prompt", "select_account")
                        .setRedirectURI(getReturnTo(provider, request))
                        .buildQueryMessage();

                response.sendRedirect(oauthRequest.getLocationUri());
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());    }
}

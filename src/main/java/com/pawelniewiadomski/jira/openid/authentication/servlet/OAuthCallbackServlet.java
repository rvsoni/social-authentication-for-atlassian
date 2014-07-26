package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.user.util.UserUtil;

import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.openid.OpenIdConnectResponse;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.OpenIdDiscoveryDocumentProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Handling OpenID Connect authentications.
 */
public class OAuthCallbackServlet extends AbstractOpenIdServlet
{
    final Logger log = Logger.getLogger(this.getClass());

    @Autowired
    GlobalSettings globalSettings;

	@Autowired
    CrowdService crowdService;

	@Autowired
    UserUtil userUtil;

	@Autowired
    LicenseProvider licenseProvider;

    @Autowired
    JiraHome jiraHome;

    @Autowired
    AuthenticationService authenticationService;

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

        final String cid = request.getParameter("cid");
        final OpenIdProvider provider;
        try {
            provider = openIdDao.findByCallbackId(cid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (provider != null) {
            try
            {
                final OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
                final String state = (String) request.getSession().getAttribute(AuthenticationService.STATE_IN_SESSION);
                if (StringUtils.equals(state, oar.getState()))
                {
                    final OpenIdDiscoveryDocumentProvider.OpenIdDiscoveryDocument discoveryDocument = notNull("OpenId Discovery Document must not be null",
                            discoveryDocumentProvider.getDiscoveryDocument(provider.getEndpointUrl()));

                    final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
                    final OAuthClientRequest oAuthRequest = OAuthClientRequest.tokenLocation(discoveryDocument.getTokenUrl())
                            .setGrantType(GrantType.AUTHORIZATION_CODE)
                            .setClientId(provider.getClientId())
                            .setClientSecret(provider.getClientSecret())
                            .setRedirectURI(getReturnTo(provider, request))
                            .setCode(oar.getCode())
                            .buildBodyMessage();

                    final OpenIdConnectResponse token = oAuthClient.accessToken(oAuthRequest, OpenIdConnectResponse.class);
                    final String accessToken = token.getAccessToken();
                    final String email = token.getIdToken().getClaimsSet().getCustomField("email", String.class);
                    String username = email;

                    final String userInfoUrl = discoveryDocument.getUserinfoUrl();
                    if (isNotEmpty(userInfoUrl))
                    {
                        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(userInfoUrl)
                                .setAccessToken(accessToken)
                                .buildQueryMessage();

                        OAuthResourceResponse userInfoResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
                        Map<String, Object> userInfo = JSONUtils.parseJSON(userInfoResponse.getBody());
                        username = defaultIfEmpty((String) userInfo.get("name"), email);
                    }

                    authenticationService.showAuthentication(request, response, provider, username, email);
                    return;
                }
            } catch (Exception e) {
                log.error("OpenID verification failed", e);
                templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
                return;
            }
        }

        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
    }
}

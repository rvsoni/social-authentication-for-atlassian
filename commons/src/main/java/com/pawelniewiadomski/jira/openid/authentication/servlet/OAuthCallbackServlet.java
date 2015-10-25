package com.pawelniewiadomski.jira.openid.authentication.servlet;


import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.google.common.collect.ImmutableMap;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.services.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.OAuth2ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.ProviderTypeFactory;
import com.pawelniewiadomski.jira.openid.authentication.services.TemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handling OpenID Connect authentications.
 */
@Slf4j
public class OAuthCallbackServlet extends HttpServlet
{
    @Autowired
    protected GlobalSettings globalSettings;

    @Autowired protected LicenseProvider licenseProvider;

    @Autowired protected AuthenticationService authenticationService;

    @Autowired protected TemplateHelper templateHelper;

    @Autowired protected OpenIdDao openIdDao;

    @Autowired protected ProviderTypeFactory providerTypeFactory;

    @Autowired protected ServletUtils servletUtils;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!licenseProvider.isValidLicense()) {
            templateHelper.render(request, response, "OpenId.Templates.invalidLicense");
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
            try {
                final OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
                final String state = (String) request.getSession().getAttribute(AuthenticationService.STATE_IN_SESSION);
                if (!StringUtils.equals(state, oar.getState())) {
                    templateHelper.render(request, response, "OpenId.Templates.invalidState",
                            ImmutableMap.<String, Object>of("providerName", provider.getName()));
                    return;
                }

                final OAuth2ProviderType providerType = (OAuth2ProviderType) providerTypeFactory.getProviderTypeById(provider.getProviderType());
                final Either<Pair<String, String>, OAuth2ProviderType.Error> userOrError = providerType.getUsernameAndEmail(oar.getCode(), provider, request);

                if (userOrError.isLeft()) {
                    Pair<String, String> usernameAndEmail = userOrError.left().get();
                    authenticationService.showAuthentication(request, response, provider, usernameAndEmail.left(), usernameAndEmail.right());
                } else {
                    final OAuth2ProviderType.Error error = userOrError.right().get();
                    templateHelper.render(request, response, "OpenId.Templates.oauthErrorWithPayload",
                            ImmutableMap.<String, Object>of("error", error));
                }
                return;
            } catch (Exception e) {
                if (e instanceof OAuthProblemException) {
                    templateHelper.render(request, response, "OpenId.Templates.oauthError",
                            ImmutableMap.<String, Object>of(
                                    "providerName", provider.getName(),
                                    "errorMessage", e.getMessage()));
                } else {
                    log.error("OpenID verification failed", e);
                    templateHelper.render(request, response, "OpenId.Templates.error");
                }
                return;
            }
        }

        templateHelper.render(request, response, "OpenId.Templates.error");
    }
}

package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.springframework.beans.factory.annotation.Autowired;

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
                    authenticationService.showAuthentication(request, response, provider, "test", "test");
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

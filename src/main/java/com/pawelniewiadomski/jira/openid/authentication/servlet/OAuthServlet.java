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
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Handling OpenID Connect authentications.
 */
public class OAuthServlet extends AbstractOpenIdServlet
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
                if (isBlank(request.getParameter("code")))
                {
                    final String returnTo = getReturnTo(provider, request);

//                    final String url = new BrowserClientRequestUrl(provider.getEndpointUrl(), provider.getClientId())
//                            .setScopes(of("email", "openid"))
//                            .setState("xyz")
//                            .setResponseTypes(of("code"))
//                            .setRedirectUri(returnTo).build();

//                    response.sendRedirect(url);
                } else {

                }
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());    }
}

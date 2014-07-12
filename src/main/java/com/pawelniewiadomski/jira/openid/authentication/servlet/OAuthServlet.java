package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.expressme.openid.OpenIdManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handling OpenID Connect authentications.
 */
public class OAuthServlet extends AbstractOpenIdServlet {
    public static final String RETURN_URL_PARAMETER = "returnUrl";
    final Logger log = Logger.getLogger(this.getClass());

    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR = ONE_HOUR * 2L;
    static final String ATTR_MAC = "openid_mac";
    static final String ATTR_ALIAS = "openid_alias";

    final Map<String, OpenIdManager> openIdConnections = Maps.newHashMap();

    @Autowired
    GlobalSettings globalSettings;

	@Autowired
    CrowdService crowdService;

	@Autowired
    UserUtil userUtil;

	@Autowired
    LicenseProvider licenseProvider;

    @Autowired
    AuthenticationService authenticationService;

    final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(final String key) throws Exception {
                    return key;
                }
            });

    /*
     * We keep separate OpenIdManagers for each of providers because we want return path to be different for each of them and
     * OpenIdManager keeps a track on return path and do checks against it (which we want because that improves security).
     */
    protected synchronized OpenIdManager getOpenIdManager(String returnTo) {
        OpenIdManager openIdManager = openIdConnections.get(returnTo);
        if (openIdManager == null) {
            openIdManager = new OpenIdManager();
            openIdManager.setReturnTo(returnTo);
            openIdConnections.put(returnTo, openIdManager);
        }
        return openIdManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!licenseProvider.isValidLicense()) {
            renderTemplate(request, response, "OpenId.Templates.invalidLicense", Collections.<String, Object>emptyMap());
            return;
        }

        final String pid = request.getParameter("pid");
        final String returnUrl = request.getParameter(RETURN_URL_PARAMETER);
        if (StringUtils.isNotBlank(returnUrl)) {
            request.getSession().setAttribute(RETURN_URL_PARAMETER, returnUrl);
        }

        final OpenIdProvider provider;
        try {
            provider = openIdDao.findProvider(Integer.valueOf(pid));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            if (provider != null) {
                final String returnTo = getReturnTo(provider, request);
                final OpenIdManager openIdManager = getOpenIdManager(returnTo);

//                if (StringUtils.isNotEmpty(nonce)) {
//                    try {
//                        // check nonce:
//                        checkNonce(nonce);
//                        // get authentication:
//                        byte[] mac_key = (byte[]) request.getSession().getAttribute(ATTR_MAC);
//                        String alias = (String) request.getSession().getAttribute(ATTR_ALIAS);
//                        Authentication authentication = openIdManager.getAuthentication(request, mac_key, alias);
//                        String fullName = authentication.getFullname();
//                        String email = authentication.getEmail();
//
//                        showAuthentication(request, response, provider, fullName, email);
//                        return;
//                    } catch (OpenIdException e) {
//                        log.error("OpenID verification failed", e);
//                        renderTemplate(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
//                        return;
//                    }
//                } else {
//                    ClientID clientID = new ClientID(applicationProperties.getString(APKeys.JIRA_TITLE));
//                    URI callback = new URI(getReturnTo(provider, request));
//                    State state = new State();
//                    AuthenticationRequest req = new AuthenticationRequest(
//                            new URI(provider.getEndpointUrl()),
//                            new ResponseType(ResponseType.Value.CODE),
//                            Scope.parse("openid email profile address"),
//                            clientID,
//                            callback,
//                            state,
//                            null);
//                    response.sendRedirect(req.toURI().toString());
//                }
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        renderTemplate(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
    }
}

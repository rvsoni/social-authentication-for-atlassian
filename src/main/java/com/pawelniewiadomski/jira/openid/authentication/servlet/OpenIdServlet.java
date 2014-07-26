package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handling OpenID 1.0 authentications.
 */
public class OpenIdServlet extends AbstractOpenIdServlet {

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

    @Autowired
    TemplateHelper templateHelper;

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
                final String returnTo = getReturnTo(provider, request);
                final OpenIdManager openIdManager = getOpenIdManager(returnTo);
                final String nonce = request.getParameter("openid.response_nonce");
                if (StringUtils.isNotEmpty(nonce)) {
                    try {
                        // check nonce:
                        checkNonce(nonce);
                        // get authentication:
                        byte[] mac_key = (byte[]) request.getSession().getAttribute(ATTR_MAC);
                        String alias = (String) request.getSession().getAttribute(ATTR_ALIAS);
                        Authentication authentication = openIdManager.getAuthentication(request, mac_key, alias);
                        String fullName = authentication.getFullname();
                        String email = authentication.getEmail();

                        authenticationService.showAuthentication(request, response, provider, fullName, email);
                        return;
                    } catch (OpenIdException e) {
                        log.error("OpenID verification failed", e);
                        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
                        return;
                    }
                } else {
                    try {
                        Endpoint endpoint = openIdManager.lookupEndpoint(provider.getEndpointUrl(), provider.getExtensionNamespace());
                        log.debug(String.format("OpenID Endpoint for %s is %s", provider.getEndpointUrl(), endpoint.toString()));

                        Association association = openIdManager.lookupAssociation(endpoint);
                        log.debug(String.format("OpenID Association for %s is %s", provider.getEndpointUrl(), association.toString()));

                        request.getSession().setAttribute(ATTR_MAC, association.getRawMacKey());
                        request.getSession().setAttribute(ATTR_ALIAS, endpoint.getAlias());
                        String url = openIdManager.getAuthenticationUrl(endpoint, association);
                        response.sendRedirect(url);
                    } catch(OpenIdException e) {
                        log.error("OpenID Authentication failed, there was an error connecting " + provider.getEndpointUrl(), e);
                        templateHelper.render(request, response, "OpenId.Templates.error",
                                ImmutableMap.<String, Object>of("sslError", e.getCause() instanceof SSLException));
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        templateHelper.render(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
    }

    void checkNonce(String nonce) {
        // check response_nonce to prevent replay-attack:
        if (nonce == null || nonce.length() < 20)
            throw new OpenIdException("Verify failed.");
        long nonceTime = getNonceTime(nonce);
        long diff = System.currentTimeMillis() - nonceTime;
        if (diff < 0)
            diff = (-diff);
        if (diff > ONE_HOUR)
            throw new OpenIdException("Bad nonce time.");
        if (isNonceExist(nonce))
            throw new OpenIdException("Verify nonce failed.");
        storeNonce(nonce, nonceTime + TWO_HOUR);
    }

    boolean isNonceExist(String nonce) {
        return cache.asMap().containsKey(nonce);
    }

    void storeNonce(String nonce, long expires) {
        cache.asMap().put(nonce, nonce);
    }

    long getNonceTime(String nonce) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .parse(nonce.substring(0, 19) + "+0000")
                    .getTime();
        } catch (ParseException e) {
            throw new OpenIdException("Bad nonce time.");
        }
    }

}

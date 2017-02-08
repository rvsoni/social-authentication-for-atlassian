package com.pawelniewiadomski.jira.openid.authentication.services;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.expressme.openid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Handling OpenID 1.0 authentications.
 */
@Service
public class OpenIdAuthenticationHandler implements AuthenticationHandler {

    final Logger log = Logger.getLogger(this.getClass());

    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR = ONE_HOUR * 2L;
    static final String ATTR_MAC = "openid_mac";
    static final String ATTR_ALIAS = "openid_alias";

    final Map<String, OpenIdManager> openIdConnections = Maps.newHashMap();

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected TemplateHelper templateHelper;

    @Autowired
    protected BaseUrlService baseUrlService;

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

    @Nonnull
    protected String getReturnTo(OpenIdProvider provider) {
        return UriBuilder.fromUri(baseUrlService.getBaseUrl())
                .path("/openid/login")
                .path(Integer.toString(provider.getID())).build().toString();
    }

    @Override
    public boolean doAuthenticationDance(OpenIdProvider provider, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String returnTo = getReturnTo(provider);
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
                return true;
            } catch (OpenIdException e) {
                log.error("OpenID verification failed", e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return true;
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
            } catch (OpenIdException e) {
                log.error("OpenID Authentication failed, there was an error connecting " + provider.getEndpointUrl(), e);
                templateHelper.render(request, response, "OpenId.Templates.error",
                        ImmutableMap.<String, Object>of("sslError", e.getCause() instanceof SSLException));
                return true;
            }
        }
        return false;
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

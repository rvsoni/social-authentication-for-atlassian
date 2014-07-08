package com.pawelniewiadomski.jira.openid.authentication.servlet;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.seraph.auth.DefaultAuthenticator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.pawelniewiadomski.jira.openid.authentication.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.LicenseProvider;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handling OpenID Connect authentications.
 */
public class OpenIdConnectServlet extends AbstractOpenIdServlet {
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
                    ClientID clientID = new ClientID(applicationProperties.getString(APKeys.JIRA_TITLE));
                    URI callback = new URI(getReturnTo(provider, request));
                    State state = new State();
                    Nonce nonce = new Nonce();
                    AuthenticationRequest req = new AuthenticationRequest(
                            new URI(provider.getEndpointUrl()),
                            new ResponseType(ResponseType.Value.CODE),
                            Scope.parse("openid email profile address"),
                            clientID,
                            callback,
                            state,
                            nonce);
                    response.sendRedirect(req.toURI().toString());
//                }
            }
        } catch (Exception e) {
            log.error("OpenID Authentication failed, there was an error: " + e.getMessage());
        }

        renderTemplate(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
    }

    void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                            final OpenIdProvider provider, String identity, String email) throws IOException, ServletException {
        if (StringUtils.isBlank(email)) {
            renderTemplate(request, response, "OpenId.Templates.emptyEmail", Collections.<String, Object>emptyMap());
            return;
        }

        if (StringUtils.isNotBlank(provider.getAllowedDomains())) {
            final String[] allowedDomains = StringUtils.split(provider.getAllowedDomains(), ',');
            final String domain = StringUtils.substringAfter(email, "@");
            boolean matchingDomain = false;
            for(final String allowedDomain : allowedDomains) {
                if (StringUtils.equals(StringUtils.trim(allowedDomain), domain)) {
                    matchingDomain = true;
                    break;
                }
            }
            if (!matchingDomain) {
                renderTemplate(request, response, "OpenId.Templates.domainMismatch", Collections.<String, Object>emptyMap());
                return;
            }
        }

        User user = (User) Iterables.getFirst(crowdService.search(new UserQuery(
                User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);

        if (user == null && !isExternalUserManagement()
                && (JiraUtils.isPublicMode() || globalSettings.isCreatingUsers())) {
            try {
                user = userUtil.createUserNoNotification(StringUtils.lowerCase(StringUtils.replaceChars(identity, " '()", "")), UUID.randomUUID().toString(),
                        email, identity);
            } catch (PermissionException e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                renderTemplate(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
                return;
            } catch (CreateException e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                renderTemplate(request, response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
                return;
            }
        }

        if (user != null) {
            final ApplicationUser appUser = ApplicationUsers.from(user);

            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
			ComponentAccessor.getComponentOfType(LoginManager.class).onLoginAttempt(request, appUser.getName(), true);

            final String returnUrl = (String) httpSession.getAttribute(RETURN_URL_PARAMETER);
            if (StringUtils.isNotBlank(returnUrl)) {
                response.sendRedirect(getBaseUrl(request) + returnUrl);
            } else {
                response.sendRedirect(getBaseUrl(request) + "/secure/Dashboard.jspa");
            }
        } else {
            renderTemplate(request, response, "OpenId.Templates.noUserMatched", Collections.<String, Object>emptyMap());
        }
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

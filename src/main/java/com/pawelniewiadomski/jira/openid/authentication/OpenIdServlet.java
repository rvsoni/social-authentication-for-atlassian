package com.pawelniewiadomski.jira.openid.authentication;


import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.expressme.openid.*;
import com.atlassian.seraph.auth.DefaultAuthenticator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public class OpenIdServlet extends HttpServlet {

    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR = ONE_HOUR * 2L;
    static final String ATTR_MAC = "openid_mac";
    static final String ATTR_ALIAS = "openid_alias";
    public static final String SOY_TEMPLATES = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin:openid-soy-templates";

    final ApplicationProperties applicationProperties;
    final CrowdService crowdService;
    final UserUtil userUtil;
    final SoyTemplateRenderer soyTemplateRenderer;


    final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(final String key) throws Exception {
                    return key;
                }
            });

    OpenIdManager manager;

    public OpenIdServlet(final ApplicationProperties applicationProperties, final CrowdService crowdService,
                         final UserUtil userUtil, final SoyTemplateRenderer soyTemplateRenderer) {
        this.applicationProperties = applicationProperties;
        this.crowdService = crowdService;
        this.userUtil = userUtil;
        this.soyTemplateRenderer = soyTemplateRenderer;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        manager = new OpenIdManager();
        final String realm = UriBuilder.fromUri(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE)).replacePath("/").build().toString();
        manager.setRealm(realm); // change to your domain
        manager.setReturnTo(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE)
                + "/plugins/servlet/open-id-authentication"); // change to your servlet url
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String op = request.getParameter("op");
        if (op == null) {
            try {
                // check nonce:
                checkNonce(request.getParameter("openid.response_nonce"));
                // get authentication:
                byte[] mac_key = (byte[]) request.getSession().getAttribute(ATTR_MAC);
                String alias = (String) request.getSession().getAttribute(ATTR_ALIAS);
                Authentication authentication = manager.getAuthentication(request, mac_key, alias);
                String identity = authentication.getIdentity();
                String email = authentication.getEmail();
                // TODO: create user if not exist in database:
                showAuthentication(request, response, identity, email);
            } catch (OpenIdException e) {
                renderTemplate(response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
            }
        } else if ("Google".equals(op)) {
            // redirect to Google sign on page:
            Endpoint endpoint = manager.lookupEndpoint("Google");
            Association association = manager.lookupAssociation(endpoint);
            request.getSession().setAttribute(ATTR_MAC, association.getRawMacKey());
            request.getSession().setAttribute(ATTR_ALIAS, endpoint.getAlias());
            String url = manager.getAuthenticationUrl(endpoint, association);
            response.sendRedirect(url);
        } else if ("Yahoo".equals(op)) {
            Endpoint endpoint = manager.lookupEndpoint("Yahoo");
            Association association = manager.lookupAssociation(endpoint);
            request.getSession().setAttribute(ATTR_MAC, association.getRawMacKey());
            request.getSession().setAttribute(ATTR_ALIAS, endpoint.getAlias());
            String url = manager.getAuthenticationUrl(endpoint, association);
            response.sendRedirect(url);
        } else {
            renderTemplate(response, "OpenId.Templates.error", Collections.<String, Object>emptyMap());
        }
    }

    void renderTemplate(final HttpServletResponse response, String template, Map<String, Object> map) throws ServletException, IOException {
        try {
            soyTemplateRenderer.render(response.getWriter(), SOY_TEMPLATES, template, map);
        } catch (SoyException e) {
            throw new ServletException(e);
        }
    }

    void showAuthentication(final HttpServletRequest request, HttpServletResponse response, String identity, String email) throws IOException {
        if (StringUtils.isBlank(email)) {
            return;
        }

        User user = (User) Iterables.getFirst(crowdService.search(new UserQuery(
                User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);

        if (user == null && Boolean.valueOf(applicationProperties.getPropertyValue(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
                && JiraUtils.isPublicMode()) {
            try {
                user = userUtil.createUserNoNotification(StringUtils.lowerCase(StringUtils.replaceChars(identity, " '()", "")), UUID.randomUUID().toString(),
                        email, identity);
            } catch (PermissionException e) {
                throw new RuntimeException(e);
            } catch (CreateException e) {
                throw new RuntimeException(e);
            }
        }

        if (user != null) {
            final ApplicationUser appUser = ApplicationUsers.from(user);

            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

            response.sendRedirect(applicationProperties.getBaseUrl() + "/secure/Dashboard.jspa");
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

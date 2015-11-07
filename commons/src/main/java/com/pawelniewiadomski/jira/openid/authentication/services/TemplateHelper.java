package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.BaseUrlHelper.getBaseUrl;
import static com.pawelniewiadomski.jira.openid.authentication.servlet.HttpCachingUtils.setNoCacheHeaders;

@Service
public class TemplateHelper
{
    public static final String SOY_TEMPLATES = "openid-soy-templates";

    @Autowired protected SoyTemplateRenderer soyTemplateRenderer;

    @Autowired protected LoginUriProvider loginUriProvider;

    @Autowired protected PluginKey pluginKey;

    protected Supplier<String> soyTemplatesResourceKey = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            return pluginKey.getKey() + ':' + SOY_TEMPLATES;
        }
    });

    public void render(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final String template) throws ServletException, IOException
    {
        render(request, response, template, Collections.<String, Object>emptyMap());
    }

    public void render(final HttpServletRequest request,
                       final HttpServletResponse response,
                       String template, Map<String, Object> map) throws ServletException, IOException
    {
        final Map<String, Object> params = Maps.newHashMap(map);
        params.put("baseUrl", getBaseUrl(request));
        try {
            params.put("loginUrl", loginUriProvider.getLoginUri(new URI("/")).toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        setNoCacheHeaders(response);
        response.setContentType("text/html; charset=UTF-8");
        try {
            soyTemplateRenderer.render(response.getWriter(), soyTemplatesResourceKey.get(), template, params);
        } catch (SoyException e) {
            throw new ServletException(e);
        }
    }

    public String render(final HttpServletRequest request,
                         String template, Map<String, Object> map) throws ServletException, IOException
    {
        final Map<String, Object> params = Maps.newHashMap(map);
        params.put("baseUrl", getBaseUrl(request));

        try {
            return soyTemplateRenderer.render(soyTemplatesResourceKey.get(), template, params);
        } catch (SoyException e) {
            throw new ServletException(e);
        }
    }

}

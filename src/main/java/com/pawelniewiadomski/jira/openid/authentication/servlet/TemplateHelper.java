package com.pawelniewiadomski.jira.openid.authentication.servlet;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.pawelniewiadomski.jira.openid.authentication.servlet.BaseUrlHelper.getBaseUrl;
import static com.pawelniewiadomski.jira.openid.authentication.servlet.HttpCachingUtils.setNoCacheHeaders;

@Service
@AllArgsConstructor
public class TemplateHelper
{
    public static final String SOY_TEMPLATES = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin:openid-soy-templates";

    final SoyTemplateRenderer soyTemplateRenderer;

    final ApplicationProperties applicationProperties;

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

        setNoCacheHeaders(response);
        response.setContentType("text/html; charset=UTF-8");
        try {
            soyTemplateRenderer.render(response.getWriter(), SOY_TEMPLATES, template, params);
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
            return soyTemplateRenderer.render(SOY_TEMPLATES, template, params);
        } catch (SoyException e) {
            throw new ServletException(e);
        }
    }

}

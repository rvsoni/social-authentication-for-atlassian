package com.pawelniewiadomski.jira.openid.authentication.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.pawelniewiadomski.jira.openid.authentication.servlet.BaseUrlHelper.getBaseUrl;

@Service
public class TemplateHelper
{
    public static final String SOY_TEMPLATES = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin:openid-soy-templates";

    @Autowired
    SoyTemplateRenderer soyTemplateRenderer;

    @Autowired
    ApplicationProperties applicationProperties;

    String getContentType()
    {
        try
        {
            return applicationProperties.getContentType();
        }
        catch (Exception e)
        {
            return "text/html; charset=UTF-8";
        }
    }

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

        JiraHttpUtils.setNoCacheHeaders(response);
        response.setContentType(getContentType());
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

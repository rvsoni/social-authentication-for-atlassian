package com.pawelniewiadomski.jira.openid.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import org.springframework.stereotype.Service;

public class BaseUrlHelper
{
    public static String getBaseUrl(HttpServletRequest request) {
        return UriBuilder.fromUri(request.getRequestURL().toString()).replacePath(request.getContextPath()).build().toString();
    }
}

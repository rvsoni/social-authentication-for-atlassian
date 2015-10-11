package com.pawelniewiadomski.jira.openid.authentication.servlet;

import javax.servlet.http.HttpServletResponse;

public class HttpCachingUtils {
    public static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1L);
    }
}

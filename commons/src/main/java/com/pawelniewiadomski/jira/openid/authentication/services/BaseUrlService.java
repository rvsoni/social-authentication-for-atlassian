package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseUrlService {
    @Autowired
    ApplicationProperties applicationProperties;

    public String getBaseUrl() {
        return applicationProperties.getBaseUrl(UrlMode.CANONICAL);
    }
}

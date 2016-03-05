package com.pawelniewiadomski.servicedesk.services;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceDeskExternalUserManagementService implements ExternalUserManagementService {

    @Autowired
    protected ApplicationProperties applicationProperties;

    public boolean isExternalUserManagement() {
        return applicationProperties != null && applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}

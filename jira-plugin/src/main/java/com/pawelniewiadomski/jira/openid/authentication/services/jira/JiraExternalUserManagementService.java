package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JiraExternalUserManagementService implements ExternalUserManagementService {

    final ApplicationProperties applicationProperties;

    public boolean isExternalUserManagement() {
        return applicationProperties != null && applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}

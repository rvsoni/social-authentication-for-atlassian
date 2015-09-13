package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import lombok.AllArgsConstructor;

@JiraComponent
@AllArgsConstructor
public class JiraExternalUserManagementService implements ExternalUserManagementService {

    @ComponentImport
    final ApplicationProperties applicationProperties;

    public boolean isExternalUserManagement() {
        return applicationProperties != null && applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}

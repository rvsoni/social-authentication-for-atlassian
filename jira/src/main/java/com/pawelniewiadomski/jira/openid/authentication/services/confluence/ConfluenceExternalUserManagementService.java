package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import org.springframework.stereotype.Service;

@ConfluenceComponent
public class ConfluenceExternalUserManagementService implements ExternalUserManagementService{
    @Override
    public boolean isExternalUserManagement() {
        return false;
    }
}

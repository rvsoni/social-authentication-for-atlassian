package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import org.springframework.stereotype.Component;

@Component
public class ConfluenceExternalUserManagementService implements ExternalUserManagementService{
    @Override
    public boolean isExternalUserManagement() {
        return false;
    }
}

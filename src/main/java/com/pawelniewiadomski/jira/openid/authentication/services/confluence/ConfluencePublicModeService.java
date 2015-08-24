package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;

@ConfluenceComponent
public class ConfluencePublicModeService implements PublicModeService {
    @Override
    public boolean canAnyoneSignUp() {
        return false;
    }
}

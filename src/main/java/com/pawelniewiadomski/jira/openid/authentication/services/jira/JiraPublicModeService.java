package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;

@JiraComponent
@AllArgsConstructor
public class JiraPublicModeService implements PublicModeService {
    @Override
    public boolean canAnyoneSignUp() {
        return JiraUtils.isPublicMode();
    }
}

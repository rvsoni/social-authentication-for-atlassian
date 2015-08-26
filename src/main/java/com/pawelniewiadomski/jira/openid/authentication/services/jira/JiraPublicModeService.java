package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@JiraComponent
@AllArgsConstructor
public class JiraPublicModeService implements PublicModeService {
    @Override
    public boolean canAnyoneSignUp() {
        return JiraUtils.isPublicMode();
    }

    @Override
    public Optional<List<String>> getAllowedDomains() {
        return Optional.empty();
    }
}

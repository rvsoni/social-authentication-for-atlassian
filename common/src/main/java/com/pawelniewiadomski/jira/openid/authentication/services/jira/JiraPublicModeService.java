package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;

import java.util.List;

@JiraComponent
@AllArgsConstructor
public class JiraPublicModeService implements PublicModeService {
    @Override
    public boolean canAnyoneSignUp() {
        return JiraUtils.isPublicMode();
    }

    @Override
    public Option<List<String>> getAllowedDomains() {
        return Option.none();
    }
}

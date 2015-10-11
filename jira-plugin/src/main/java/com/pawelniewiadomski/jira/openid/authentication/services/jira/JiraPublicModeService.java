package com.pawelniewiadomski.jira.openid.authentication.services.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.JiraUtils;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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

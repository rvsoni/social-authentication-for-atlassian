package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.confluence.user.SignupManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@ConfluenceComponent
@AllArgsConstructor
public class ConfluencePublicModeService implements PublicModeService {
    @ComponentImport
    final SignupManager signupManager;

    @Override
    public boolean canAnyoneSignUp() {
        return signupManager.isPublicSignupPermitted();
    }
}

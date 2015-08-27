package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.confluence.user.SignupManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.split;

@ConfluenceComponent
@AllArgsConstructor
public class ConfluencePublicModeService implements PublicModeService {
    @ComponentImport
    final SignupManager signupManager;

    @Override
    public boolean canAnyoneSignUp() {
        return signupManager.isPublicSignupPermitted();
    }

    @Override
    public Optional<List<String>> getAllowedDomains() {
        return Optional.of(ofNullable(split(signupManager.getRestrictedDomains(), ','))
                .map(new Function<String[], ImmutableList<String>>() {
                    @Override
                    public ImmutableList<String> apply(String[] elements) {
                        return ImmutableList.copyOf(elements);
                    }
                }).orElse(ImmutableList.<String>of()));
    }
}

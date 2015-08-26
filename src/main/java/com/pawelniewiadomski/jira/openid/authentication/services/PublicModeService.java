package com.pawelniewiadomski.jira.openid.authentication.services;

import java.util.List;
import java.util.Optional;

public interface PublicModeService {
    boolean canAnyoneSignUp();

    /**
     *
     * @return {@link Optional#empty()} if given product doesn't support allowed domains.
     */
    Optional<List<String>> getAllowedDomains();
}

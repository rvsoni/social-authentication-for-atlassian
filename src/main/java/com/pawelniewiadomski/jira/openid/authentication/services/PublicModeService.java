package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.fugue.Option;

import java.util.List;

public interface PublicModeService {
    boolean canAnyoneSignUp();

    /**
     *
     * @return {@link Option#none()} if given product doesn't support allowed domains.
     */
    Option<List<String>> getAllowedDomains();
}

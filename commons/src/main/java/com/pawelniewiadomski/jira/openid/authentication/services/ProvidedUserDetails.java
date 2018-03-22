package com.pawelniewiadomski.jira.openid.authentication.services;

import lombok.NonNull;
import lombok.Value;

@Value
public class ProvidedUserDetails {
    @NonNull String identity;
    @NonNull String email;
}

package com.pawelniewiadomski.jira.openid.authentication.services;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface HomePageService {
    String getHomePagePath();
}

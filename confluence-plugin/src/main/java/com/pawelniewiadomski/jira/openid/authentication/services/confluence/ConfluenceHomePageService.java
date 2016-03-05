package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.pawelniewiadomski.jira.openid.authentication.services.HomePageService;
import org.springframework.stereotype.Component;

@Component
public class ConfluenceHomePageService implements HomePageService {
    @Override
    public String getHomePagePath() {
        return "/index.action";
    }
}

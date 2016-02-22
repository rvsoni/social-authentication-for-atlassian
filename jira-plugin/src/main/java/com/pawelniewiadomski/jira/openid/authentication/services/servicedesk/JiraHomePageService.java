package com.pawelniewiadomski.jira.openid.authentication.services.servicedesk;

import com.pawelniewiadomski.jira.openid.authentication.services.HomePageService;
import org.springframework.stereotype.Component;

@Component
public class JiraHomePageService implements HomePageService {
    @Override
    public String getHomePagePath() {
        return "/secure/Dashboard.jspa";
    }
}

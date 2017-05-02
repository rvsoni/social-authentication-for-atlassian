package com.pawelniewiadomski.jira.openid.authentication.services;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    @Autowired
    protected AnalyticsConfigService analyticsConfigService;

    public void reportProviders() {
        GoogleAnalytics ga = new GoogleAnalytics("UA-1934760-16");
        ga.postAsync(new EventHit("Booting up", "Test", "Label", 0));
    }
}

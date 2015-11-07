package com.pawelniewiadomski.jira.openid.authentication.services.servicedesk;

import com.atlassian.fugue.Option;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.opensymphony.module.propertyset.PropertySet;
import com.pawelniewiadomski.jira.openid.authentication.services.PublicModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceDeskPublicModeService implements PublicModeService {
    protected String ENTITY_NAME = "vp.properties";
    protected Long ENTITY_ID = 1L;
    protected String PUBLIC_SIGNUP_KEY = "com.atlassian.servicedesk.public.signup.enabled";
    protected JiraPropertySetFactory jiraPropertySetFactory;

    @Autowired
    public ServiceDeskPublicModeService(JiraPropertySetFactory jiraPropertySetFactory) {
        this.jiraPropertySetFactory = jiraPropertySetFactory;
    }

    // need to take the setting from
    // 10518	vp.properties	1	com.atlassian.servicedesk.public.signup.enabled	1
    @Override
    public boolean canAnyoneSignUp() {
        final PropertySet propertSet = jiraPropertySetFactory.buildCachingPropertySet(ENTITY_NAME, ENTITY_ID, false);

        return propertSet.exists(PUBLIC_SIGNUP_KEY) && propertSet.getBoolean(PUBLIC_SIGNUP_KEY);
    }

    @Override
    public Option<List<String>> getAllowedDomains() {
        return Option.none();
    }
}

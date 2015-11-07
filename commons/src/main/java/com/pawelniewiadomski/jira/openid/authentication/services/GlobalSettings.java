package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalSettings {

    public static final String SHOULD_CREATE_USERS = "should.create.users";

    @Autowired
    protected PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    protected PublicModeService publicModeService;

    public boolean isCreatingUsers() {
        return publicModeService.canAnyoneSignUp()
                || Boolean.valueOf((String) pluginSettingsFactory.createGlobalSettings().get(SHOULD_CREATE_USERS));
    }

    public void setCreatingUsers(boolean createUsers) {
        pluginSettingsFactory.createGlobalSettings().put(SHOULD_CREATE_USERS, Boolean.toString(createUsers));
    }
}

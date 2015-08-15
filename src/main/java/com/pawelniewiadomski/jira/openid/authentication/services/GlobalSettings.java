package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class GlobalSettings {

    public static final String SHOULD_CREATE_USERS = "should.create.users";

    final PluginSettingsFactory pluginSettingsFactory;

    public GlobalSettings(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public boolean isCreatingUsers() {
        return isJiraPublicMode() || Boolean.valueOf((String) pluginSettingsFactory.createGlobalSettings().get(SHOULD_CREATE_USERS));
    }

    private boolean isJiraPublicMode() {
        try {
            return JiraUtils.isPublicMode();
        } catch(Exception e) {
            return false;
        }
    }

    public void setCreatingUsers(boolean createUsers) {
        pluginSettingsFactory.createGlobalSettings().put(SHOULD_CREATE_USERS, Boolean.toString(createUsers));
    }
}

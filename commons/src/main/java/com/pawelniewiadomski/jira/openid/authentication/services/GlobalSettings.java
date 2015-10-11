package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GlobalSettings {

    public static final String SHOULD_CREATE_USERS = "should.create.users";

    final PluginSettingsFactory pluginSettingsFactory;

    final PublicModeService publicModeService;

    public boolean isCreatingUsers() {
        return publicModeService.canAnyoneSignUp()
                || Boolean.valueOf((String) pluginSettingsFactory.createGlobalSettings().get(SHOULD_CREATE_USERS));
    }

    public void setCreatingUsers(boolean createUsers) {
        pluginSettingsFactory.createGlobalSettings().put(SHOULD_CREATE_USERS, Boolean.toString(createUsers));
    }
}

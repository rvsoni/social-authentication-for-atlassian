package com.pawelniewiadomski.jira.openid.authentication.conditions;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Preconditions;

import java.util.Map;

public class CanAccessConfigurationCondition implements Condition {
    private final UserManager userManager;

    public CanAccessConfigurationCondition(@ComponentImport UserManager userManager) {
        this.userManager = Preconditions.checkNotNull(userManager);
    }

    public void init(Map<String, String> params) throws PluginParseException {
    }

    public boolean shouldDisplay(Map<String, Object> context) {
        String userName = this.userManager.getRemoteUsername();
        return userName != null && this.userManager.isSystemAdmin(userName);
    }
}

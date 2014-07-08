package com.pawelniewiadomski.jira.openid.authentication.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.springframework.beans.factory.annotation.Autowired;

public class SwitchGoogleToConnectUpgradeTask implements PluginUpgradeTask {
    @Autowired
    OpenIdDao openIdDao;

    @Override
    public int getBuildNumber() {
        return 3;
    }

    @Override
    public String getShortDescription() {
        return "Switches Google to use OpenID Connect";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        final OpenIdProvider provider = openIdDao.findByName(LoadDefaultProvidersComponent.GOOGLE);
        if (provider != null)
        {
            provider.setConnect(true);
            provider.setEndpointUrl("https://accounts.google.com/o/oauth2/auth");
            provider.save();
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey() {
        return PluginKey.KEY;
    }
}

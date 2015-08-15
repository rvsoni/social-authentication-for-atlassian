package com.pawelniewiadomski.jira.openid.authentication.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;

import org.springframework.beans.factory.annotation.Autowired;

public class ProviderTypeUpgradeTask implements PluginUpgradeTask {

    final OpenIdDao openIdDao;

    public ProviderTypeUpgradeTask(OpenIdDao openIdDao) {
        this.openIdDao = openIdDao;
    }

    @Override
    public int getBuildNumber() {
        return 10;
    }

    @Override
    public String getShortDescription() {
        return "Set provider type for existing OpenID providers";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        for(OpenIdProvider provider : openIdDao.findAllProviders()) {
            provider.setProviderType(OpenIdProvider.OPENID_TYPE);
            provider.save();
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey() {
        return PluginKey.KEY;
    }
}

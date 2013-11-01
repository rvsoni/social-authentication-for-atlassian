package com.pawelniewiadomski.jira.openid.authentication.upgrade;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;

public class AddOrderUpgradeTask implements PluginUpgradeTask {

    @Autowired
    OpenIdDao openIdDao;

    @Override
    public int getBuildNumber() {
        return 2;
    }

    @Override
    public String getShortDescription() {
        return "Set ordering of OpenID providers";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        int order = 0;
        for(OpenIdProvider provider : openIdDao.findAllProviders()) {
            provider.setOrdering(order++);
            provider.save();
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey() {
        return PluginKey.KEY;
    }
}

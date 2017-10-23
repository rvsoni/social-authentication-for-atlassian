package com.pawelniewiadomski.jira.openid.authentication.upgrade;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.services.ProviderTypeFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component
public class ProviderNamesUpgradeTask implements PluginUpgradeTask {

    private final OpenIdDao openIdDao;
    private final PluginKey pluginKey;
    private final ProviderTypeFactory providerTypeFactory;

    public ProviderNamesUpgradeTask(OpenIdDao openIdDao, PluginKey pluginKey, ProviderTypeFactory providerTypeFactory) {
        this.openIdDao = openIdDao;
        this.pluginKey = pluginKey;
        this.providerTypeFactory = providerTypeFactory;
    }

    @Override
    public int getBuildNumber() {
        return 11;
    }

    @Override
    public String getShortDescription() {
        return "Set provider names for existing OpenID providers";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        for(OpenIdProvider provider : openIdDao.findAllProviders()) {
            if (isBlank(provider.getName())) {
                final ProviderType providerType = providerTypeFactory.getProviderTypeById(provider.getProviderType());
                if (isNotBlank(providerType.getDefaultName())) {
                    provider.setName(providerType.getDefaultName());
                    provider.save();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey() {
        return pluginKey.getKey();
    }
}

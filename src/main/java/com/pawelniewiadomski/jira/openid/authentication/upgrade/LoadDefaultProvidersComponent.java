package com.pawelniewiadomski.jira.openid.authentication.upgrade;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class LoadDefaultProvidersComponent implements PluginUpgradeTask {
    public static final String GOOGLE = "Google";

    @Autowired
    OpenIdDao openIdDao;

	@Override
	public int getBuildNumber() {
		return 1;
	}

	@Override
	public String getShortDescription() {
		return "Create list of default OpenID providers";
	}

	@Override
	public Collection<Message> doUpgrade() throws Exception {
        final OpenIdProvider google = openIdDao.createProvider(GOOGLE, "https://www.google.com/accounts/o8/id", "ext1", true);
        google.setEnabled(true);
        google.save();

        final OpenIdProvider yahoo = openIdDao.createProvider(YahooProvider.NAME, YahooProvider.ENDPOINT_URL, YahooProvider.EXTENSION_NAMESPACE, true);
        yahoo.setEnabled(false);
        yahoo.save();
		return Collections.emptyList();
	}

	@Override
	public String getPluginKey() {
		return PluginKey.KEY;
	}
}

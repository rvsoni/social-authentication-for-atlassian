package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class LoadDefaultProvidersComponent implements PluginUpgradeTask {
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
		openIdDao.createProvider("Google", "https://www.google.com/accounts/o8/id", "ext1", true);
		openIdDao.createProvider("Yahoo!", "http://open.login.yahooapis.com/openid20/www.yahoo.com/xrds", "ax", true);
		return Collections.emptyList();
	}

	@Override
	public String getPluginKey() {
		return PluginKey.KEY;
	}
}

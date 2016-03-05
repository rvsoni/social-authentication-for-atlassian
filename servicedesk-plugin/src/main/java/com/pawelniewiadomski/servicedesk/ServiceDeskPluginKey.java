package com.pawelniewiadomski.servicedesk;

import com.pawelniewiadomski.jira.openid.authentication.PluginKey;
import org.springframework.stereotype.Component;

@Component
public class ServiceDeskPluginKey implements PluginKey {

	public static final String KEY = "easy.social.sign-ups.servicedesk";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String configurationContext() {
		return "jira-servicedesk-configuration";
	}

	public String getRestKey() {
		return "easy-sign-ups";
	}

	@Override
	public String getCallbackPath() {
		return "/easy-sign-ups/oauth2-callback";
	}

	@Override
	public boolean areCustomProvidersDisabled() {
		return true;
	}
}

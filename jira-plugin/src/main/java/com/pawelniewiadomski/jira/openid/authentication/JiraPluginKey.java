package com.pawelniewiadomski.jira.openid.authentication;

import org.springframework.stereotype.Component;

@Component
public class JiraPluginKey implements PluginKey {

	public static final String KEY = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String configurationContext() {
		return "jira-openid-configuration";
	}
}

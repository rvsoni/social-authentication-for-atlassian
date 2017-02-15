package com.pawelniewiadomski.jira.openid.authentication;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface PluginKey {
	String getKey();
	String configurationContext();
	String getRestKey();
	String getCallbackPath();
	boolean areCustomProvidersDisabled();
	String getSslConfigurationTutorial();
}

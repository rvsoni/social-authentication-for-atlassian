package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Slf4j
@Service
public class AnalyticsConfigService {
    private static final String ANALYTICS_PREFIX = "com.atlassian.analytics.client.configuration.";

    @Autowired
    protected PluginSettingsFactory pluginSettingsFactory;

    public boolean canCollectAnalytics() {
        return (isAnalyticsEnabled() && isPolicyUpdateAcknowledged());
    }

    public boolean isAnalyticsEnabled() {
        final String analyticsEnabled = getSetting(Key.ANALYTICS_ENABLED);
        return isNotEmpty(analyticsEnabled) && Boolean.parseBoolean(analyticsEnabled);
    }

    public boolean isPolicyUpdateAcknowledged() {
        final String policyAcknowledged = getSetting(Key.POLICY_ACKNOWLEDGED);
        return isNotEmpty(policyAcknowledged) && Boolean.parseBoolean(policyAcknowledged);
    }

    public enum Key {
        POLICY_ACKNOWLEDGED("policy_acknowledged"),
        ANALYTICS_ENABLED("analytics_enabled");

        private String key;

        Key(String suffix) {
            this.key = ANALYTICS_PREFIX + "." + suffix;
        }

        public String getKey() {
            return key;
        }
    }

    private String getSetting(Key key) {
        try {
            return (String) pluginSettingsFactory.createGlobalSettings().get(key.getKey());
        } catch (RuntimeException e) {
            log.warn("Couldn't check the analytics settings. This can safely be ignored during plugin shutdown. Detail: " + e.getMessage());
        }
        return null;
    }
}

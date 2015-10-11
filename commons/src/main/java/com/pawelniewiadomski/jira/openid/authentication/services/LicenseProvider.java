package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class LicenseProvider {
    @Autowired
    protected PluginLicenseManager pluginLicenseManager;

    public boolean isValidLicense() {
        final PluginLicense license = pluginLicenseManager.getLicense().getOrElse((PluginLicense) null);
        return license != null && license.isValid();
    }
}

package com.pawelniewiadomski.jira.openid.authentication;

import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;
import com.google.common.collect.Iterables;

/**
 *
 */
public class LicenseProvider {

    public boolean isValidLicense() {
        try {
            try {
                final ThirdPartyPluginLicenseStorageManager licenseManager = Iterables.<ThirdPartyPluginLicenseStorageManager>getFirst(
                        SpringContext.getApplicationContext().getBeansOfType(ThirdPartyPluginLicenseStorageManager.class).values(), null);
                if (licenseManager.getLicense().isDefined())
                {
                    for (PluginLicense pluginLicense : licenseManager.getLicense())
                    {
                        return !pluginLicense.getError().isDefined();
                    }
                }
            } catch (PluginLicenseStoragePluginUnresolvedException e) {
                return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
        return false;
    }

}

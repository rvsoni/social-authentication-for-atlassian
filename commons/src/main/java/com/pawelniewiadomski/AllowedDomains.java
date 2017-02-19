package com.pawelniewiadomski;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;

@UtilityClass
public class AllowedDomains {
    public static boolean isEmailFromAllowedDomain(OpenIdProvider provider, String email) {
        final String[] allowedDomains = StringUtils.split(provider.getAllowedDomains(), ',');
        final String domain = StringUtils.substringAfter(email, "@");
        boolean matchingDomain = false;
        for (final String allowedDomain : allowedDomains) {
            if (StringUtils.equals(StringUtils.trim(allowedDomain), domain)) {
                matchingDomain = true;
                break;
            }
        }
        return matchingDomain;
    }
}
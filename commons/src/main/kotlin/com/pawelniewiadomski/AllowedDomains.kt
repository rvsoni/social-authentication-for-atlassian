package com.pawelniewiadomski

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider
import org.apache.commons.lang.StringUtils

class AllowedDomains {
    companion object {
        @JvmStatic
        fun isEmailFromAllowedDomain(provider: OpenIdProvider, email: String): Boolean {
            val allowedDomains = StringUtils.split(provider.allowedDomains, ',')
            val domain = StringUtils.substringAfter(email, "@")
            var matchingDomain = false
            for (allowedDomain in allowedDomains) {
                if (StringUtils.equals(StringUtils.trim(allowedDomain), domain)) {
                    matchingDomain = true
                    break
                }
            }
            return matchingDomain
        }
    }
}
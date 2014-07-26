package com.pawelniewiadomski.jira.openid.authentication.openid;

import org.apache.oltu.oauth2.client.validator.OAuthClientValidator;

public class DiscoveryValidator extends OAuthClientValidator
{
    public DiscoveryValidator() {
        requiredParams.put("authorization_endpoint", new String[] {});
        requiredParams.put("token_endpoint", new String[] {});
        requiredParams.put("userinfo_endpoint", new String[]{});
    }
}

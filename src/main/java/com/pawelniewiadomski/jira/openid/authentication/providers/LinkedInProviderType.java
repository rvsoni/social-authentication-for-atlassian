package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.common.OAuthProviderType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LinkedInProviderType extends AbstractOAuth2ProviderType {

    public LinkedInProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getAuthorizationUrl() {
        return OAuthProviderType.LINKEDIN.getAuthzEndpoint();
    }

    @Nonnull
    @Override
    public String getCallbackId() {
        return "linkedin";
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.LINKED_IN_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.linkedin");
    }
}
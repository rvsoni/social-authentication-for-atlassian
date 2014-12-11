package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.common.OAuthProviderType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoogleProviderType extends AbstractOAuth2ProviderType {

    public GoogleProviderType(I18nResolver i18nResolver, OpenIdDao openIdDao) {
        super(i18nResolver, openIdDao);
    }

    @Nonnull
    @Override
    public String getId() {
        return OpenIdProvider.GOOGLE_TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return i18nResolver.getText("openid.provider.type.google");
    }

    @Nonnull
    @Override
    public String getAuthorizationUrl() {
        return OAuthProviderType.GOOGLE.getAuthzEndpoint();
    }

    @Nonnull
    @Override
    public String getCallbackId() {
        return "google";
    }

    @Nullable
    @Override
    public String getCreatedProviderName() {
        return "Google";
    }
}
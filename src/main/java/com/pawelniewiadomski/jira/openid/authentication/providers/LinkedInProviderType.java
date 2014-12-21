package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.atlassian.jira.util.lang.Pair;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import static com.pawelniewiadomski.jira.openid.authentication.OpenIdConnectReturnToHelper.getReturnTo;

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

    @Override
    public OAuthClientRequest createOAuthRequest(@Nonnull OpenIdProvider provider,
                                                 @Nonnull String state,
                                                 @Nonnull HttpServletRequest request) throws Exception {
        return OAuthClientRequest
                .authorizationLocation(OAuthProviderType.LINKEDIN.getAuthzEndpoint())
                .setClientId(provider.getClientId())
                .setResponseType(ResponseType.CODE.toString())
                .setState(state)
                .setScope("openid email profile")
                .setParameter("prompt", "select_account")
                .setRedirectURI(getReturnTo(provider, request))
                .buildQueryMessage();
    }

    @Override
    public Pair<String, String> getUsernameAndEmail(@Nonnull String code, @Nonnull OpenIdProvider provider, HttpServletRequest request) throws Exception {
        return null;
    }
}
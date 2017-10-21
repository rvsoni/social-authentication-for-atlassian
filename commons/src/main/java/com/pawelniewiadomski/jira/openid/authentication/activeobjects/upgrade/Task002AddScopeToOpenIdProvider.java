package com.pawelniewiadomski.jira.openid.authentication.activeobjects.upgrade;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.pawelniewiadomski.jira.openid.authentication.providers.DiscoverablyOauth2ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.java.ao.Query;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@Slf4j
public class Task002AddScopeToOpenIdProvider implements ActiveObjectsUpgradeTask {

    @Override
    public ModelVersion getModelVersion() {
        return ModelVersion.valueOf("2");
    }

    @Override
    public void upgrade(@Nonnull final ModelVersion modelVersion, @Nonnull final ActiveObjects ao) {
        ao.migrate(OpenIdProviderV3.class);

        final String queryString = OpenIdProviderV3.SCOPE + " IS NULL AND "
                + OpenIdProviderV3.PROVIDER_TYPE + "=" + OpenIdProviderV3.OAUTH2_TYPE;

        for (final OpenIdProviderV3 provider : ao.find(OpenIdProviderV3.class, Query.select().where(queryString))) {
            if (provider.getScope() == null) {
                provider.setScope(DiscoverablyOauth2ProviderType.DEFAULT_SCOPE);
                provider.save();
            }
        }
    }
}

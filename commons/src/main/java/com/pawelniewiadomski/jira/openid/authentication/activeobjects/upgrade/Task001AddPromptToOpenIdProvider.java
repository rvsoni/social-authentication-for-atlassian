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
public class Task001AddPromptToOpenIdProvider implements ActiveObjectsUpgradeTask {

    @Override
    public ModelVersion getModelVersion() {
        return ModelVersion.valueOf("2");
    }

    @Override
    public void upgrade(@Nonnull final ModelVersion modelVersion, @Nonnull final ActiveObjects ao) {
        ao.migrate(OpenIdProviderV2.class);

        final String queryString = OpenIdProviderV2.PROMPT + " IS NULL";
        for (final OpenIdProviderV2 provider : ao.find(OpenIdProviderV2.class, Query.select().where(queryString))) {
            if (provider.getPrompt() == null) {
                provider.setPrompt(DiscoverablyOauth2ProviderType.SELECT_ACCOUNT_PROMPT);
                provider.save();
            }
        }
    }
}

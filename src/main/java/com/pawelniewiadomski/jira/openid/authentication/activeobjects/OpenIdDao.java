package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import net.java.ao.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OpenIdDao {

    @ComponentImport
    final ActiveObjects activeObjects;

    @Nonnull
    public List<OpenIdProvider> findAllProviders() throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select().order(String.format("%s, %s", OpenIdProvider.ORDERING, OpenIdProvider.NAME)));
        if (providers != null && providers.length > 0) {
            return Arrays.asList(providers);
        }
	  	return Collections.emptyList();
    }

    @Nonnull
    public int getNextOrdering() {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select().order(String.format("%s DESC, %s DESC", OpenIdProvider.ORDERING, OpenIdProvider.NAME)).limit(1));
        if (providers != null && providers.length > 0) {
            return providers[providers.length-1].getOrdering() + 1;
        }
        return 1;
    }

    @Nullable
    public OpenIdProvider findByName(@Nonnull String name) throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
				Query.select().where(String.format("%s = ?", OpenIdProvider.NAME), name));
        if (providers != null && providers.length > 0) {
            return providers[0];
        }
        return null;
    }

    @Nullable
    public OpenIdProvider findByCallbackId(@Nonnull String cid) throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select().where(String.format("%s = ?", OpenIdProvider.CALLBACK_ID), cid));
        if (providers != null && providers.length > 0) {
            return providers[0];
        }
        return null;
    }

    public OpenIdProvider createProvider(@Nonnull Map<String, Object> params) throws SQLException {
        return activeObjects.executeInTransaction(new TransactionCallback<OpenIdProvider>() {
            @Override
            public OpenIdProvider doInTransaction() {
                return activeObjects.create(OpenIdProvider.class,
                        ImmutableMap.<String, Object>builder()
                                .putAll(params)
                                .put(OpenIdProvider.ORDERING, OpenIdDao.this.getNextOrdering())
                                .put(OpenIdProvider.ENABLED, true).build());
            }
        });
    }

    public void deleteProvider(Integer id) throws SQLException {
        OpenIdProvider provider = findProvider(id);
        if (provider != null) {
            activeObjects.executeInTransaction(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction() {
                    activeObjects.delete(provider);
                    return null;
                }
            });
        }
    }

    @Nullable
    public OpenIdProvider findProvider(Integer id) throws SQLException {
        return activeObjects.get(OpenIdProvider.class, id);
    }

    @Nonnull
    public List<OpenIdProvider> findAllEnabledProviders() throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select()
                        .where(String.format("%s = ?", OpenIdProvider.ENABLED), Boolean.TRUE)
                        .order(String.format("%s, %s", OpenIdProvider.ORDERING, OpenIdProvider.NAME)));

        if (providers != null && providers.length > 0) {
            return Arrays.asList(providers);
        }

        return Collections.emptyList();
    }
}

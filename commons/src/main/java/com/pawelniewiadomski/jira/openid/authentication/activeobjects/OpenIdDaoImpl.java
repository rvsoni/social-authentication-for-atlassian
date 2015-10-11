package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import lombok.AllArgsConstructor;
import net.java.ao.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;

@Service
@AllArgsConstructor
public class OpenIdDaoImpl implements OpenIdDao {

    final ActiveObjects activeObjects;

    @Override
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
            return providers[providers.length - 1].getOrdering() + 1;
        }
        return 1;
    }

    @Override
    @Nullable
    public OpenIdProvider findByName(@Nonnull String name) throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select().where(String.format("%s = ?", OpenIdProvider.NAME), name));
        if (providers != null && providers.length > 0) {
            return providers[0];
        }
        return null;
    }

    @Override
    @Nullable
    public OpenIdProvider findByCallbackId(@Nonnull String cid) throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
                Query.select().where(String.format("%s = ?", OpenIdProvider.CALLBACK_ID), cid));
        if (providers != null && providers.length > 0) {
            return providers[0];
        }
        return null;
    }

    @Override
    public OpenIdProvider saveProvider(@Nonnull OpenIdProvider provider) throws SQLException {
        provider.save();
        return provider;
    }

    @Override
    public OpenIdProvider createProvider(@Nonnull Map<String, Object> params) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.putAll(params);
        map.put(OpenIdProvider.ORDERING, OpenIdDaoImpl.this.getNextOrdering());
        map.put(OpenIdProvider.ENABLED, true);
        return activeObjects.create(OpenIdProvider.class, map);
    }

    @Override
    public void deleteProvider(Integer id) throws SQLException {
        OpenIdProvider provider = findProvider(id);
        if (provider != null) {
            activeObjects.delete(provider);
        }
    }

    @Override
    @Nullable
    public OpenIdProvider findProvider(Integer id) throws SQLException {
        return activeObjects.get(OpenIdProvider.class, id);
    }

    @Override
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

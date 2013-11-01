package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class OpenIdDao {

    @Autowired
    protected ActiveObjects activeObjects;

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
    public int countAllProviders() {
        return activeObjects.count(OpenIdProvider.class);
    }

    @Nullable
    public OpenIdProvider findByName(String name) throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class,
				Query.select().where(String.format("%s = ?", OpenIdProvider.NAME), name));
        if (providers != null && providers.length > 0) {
            return providers[0];
        }
        return null;
    }

    public OpenIdProvider createProvider(String name, String url, String namespace) throws SQLException {
        return createProvider(name, url, namespace, false);
    }

    public OpenIdProvider createProvider(String name, String url, String namespace, boolean internal) throws SQLException {
        return activeObjects.create(OpenIdProvider.class,
                new DBParam(OpenIdProvider.NAME, name),
                new DBParam(OpenIdProvider.ENDPOINT_URL, url),
                new DBParam(OpenIdProvider.ENABLED, true),
                new DBParam(OpenIdProvider.EXTENSION_NAMESPACE, namespace),
                new DBParam(OpenIdProvider.INTERNAL, internal),
                new DBParam(OpenIdProvider.ORDERING, countAllProviders()));
    }

    public void deleteProvider(Integer id) throws SQLException {
        OpenIdProvider provider = findProvider(id);
        if (provider != null) {
            activeObjects.delete(provider);
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
                    .where(String.format("%s = true", OpenIdProvider.ENABLED))
                    .order(String.format("%s, %s", OpenIdProvider.ORDERING, OpenIdProvider.NAME)));

        if (providers != null && providers.length > 0) {
            return Arrays.asList(providers);
        }

        return Collections.emptyList();
    }
}

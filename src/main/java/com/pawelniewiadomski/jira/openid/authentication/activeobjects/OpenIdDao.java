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
import java.util.List;

@Component
public class OpenIdDao {

    @Autowired
    protected ActiveObjects activeObjects;

    @Nonnull
    public List<OpenIdProvider> findAllProviders() throws SQLException {
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class);
        if (providers != null && providers.length > 0) {
            return Arrays.asList(providers);
        }

        createProvider("Google", "https://www.google.com/accounts/o8/id", "ext1", true);
        createProvider("Yahoo!", "http://open.login.yahooapis.com/openid20/www.yahoo.com/xrds", "ax", true);

        return findAllProviders();
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
                new DBParam(OpenIdProvider.INTERNAL, internal));
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

}

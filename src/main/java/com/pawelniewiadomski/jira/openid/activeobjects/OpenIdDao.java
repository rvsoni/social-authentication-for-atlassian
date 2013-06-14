package com.pawelniewiadomski.jira.openid.activeobjects;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.DBParam;
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
        final OpenIdProvider[] providers = activeObjects.find(OpenIdProvider.class);
        if (providers != null && providers.length > 0) {
            return Arrays.asList(providers);
        }
        return Collections.emptyList();
    }

    public OpenIdProvider createProvider(String name, String url, String namespace) throws SQLException {
        return activeObjects.create(OpenIdProvider.class,
                new DBParam(OpenIdProvider.NAME, name),
                new DBParam(OpenIdProvider.ENDPOINT_URL, url),
                new DBParam(OpenIdProvider.ENABLED, true),
                new DBParam(OpenIdProvider.EXTENSION_NAMESPACE, namespace));
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

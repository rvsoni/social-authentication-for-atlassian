package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import com.atlassian.activeobjects.tx.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Transactional
public interface OpenIdDao {
    @Nonnull
    List<OpenIdProvider> findAllProviders() throws SQLException;

    @Nullable
    OpenIdProvider findByName(@Nonnull String name) throws SQLException;

    @Nullable
    OpenIdProvider findByCallbackId(@Nonnull String cid) throws SQLException;

    OpenIdProvider createProvider(@Nonnull Map<String, Object> params) throws SQLException;

    void deleteProvider(Integer id) throws SQLException;

    @Nullable
    OpenIdProvider findProvider(Integer id) throws SQLException;

    @Nonnull
    List<OpenIdProvider> findAllEnabledProviders() throws SQLException;
}

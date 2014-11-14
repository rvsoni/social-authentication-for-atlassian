package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class ProviderValidator {

    @Autowired
    OpenIdDao openIdDao;

    @Autowired
    I18nResolver i18nResolver;

    @Autowired
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @Nonnull
    public ErrorCollection validateCreate(ProviderBean providerBean) {
        return validateUpdate(null, providerBean);
    }

    @Nonnull
    public ErrorCollection validateUpdate(@Nullable OpenIdProvider provider, @Nonnull ProviderBean providerBean) {
        ErrorCollection errors = new SimpleErrorCollection();

        if (isEmpty(providerBean.getName())) {
            errors.addError("name", i18nResolver.getText("configuration.name.empty"));
        } else {
            final OpenIdProvider providerByName;
            try {
                providerByName = openIdDao.findByName(providerBean.getName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (providerByName != null && (provider == null || providerByName.getID() != provider.getID())) {
                errors.addError("name", i18nResolver.getText("configuration.name.must.be.unique"));
            }
        }

        if (isEmpty(providerBean.getEndpointUrl())) {
            errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty"));
        }

        if (providerBean.getProviderType().equals(OpenIdProvider.OAUTH2_TYPE)) {
            if (isEmpty(providerBean.getEndpointUrl())) {
                errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.empty", providerBean.getEndpointUrl()));
            } else {
                try {
                    discoveryDocumentProvider.getDiscoveryDocument(providerBean.getEndpointUrl());
                } catch (Exception e) {
                    errors.addError("endpointUrl", i18nResolver.getText("configuration.endpointUrl.discovery.missing", providerBean.getEndpointUrl()));
                }
            }
            if (isEmpty(providerBean.getClientId())) {
                errors.addError("clientId", i18nResolver.getText("configuration.clientId.empty"));
            }
            if (isEmpty(providerBean.getClientSecret())) {
                errors.addError("clientSecret", i18nResolver.getText("configuration.clientSecret.empty"));
            }
            if (isEmpty(providerBean.getCallbackId())) {
                errors.addErrorMessage(i18nResolver.getText("configuration.callbackId.empty"));
            }
        } else {
            if (isEmpty(providerBean.getExtensionNamespace())) {
                errors.addError("extensionNamespace", i18nResolver.getText("configuration.extensionNamespace.empty"));
            }
        }
        return errors;
    }
}

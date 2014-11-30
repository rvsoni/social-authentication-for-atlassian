package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.fugue.Either;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;

@Service
public class ProviderValidator {

    @Autowired
    OpenIdDao openIdDao;

    @Autowired
    I18nResolver i18nResolver;

    @Autowired
    OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    @Autowired
    ProviderTypeFactory providerTypeFactory;

    @Nonnull
    public Either<ErrorCollection, OpenIdProvider> validateCreate(ProviderBean providerBean) {
        final ProviderType providerType = providerTypeFactory.getProviderTypeById(providerBean.getProviderType());

        final Either<ErrorCollection, Map<String, Object>> errorsOrProvider = providerType.validateCreate(providerBean);
        if (errorsOrProvider.isLeft()) {
            return Either.left(errorsOrProvider.left().get());
        } else {
            try {
                return Either.right(openIdDao.createProvider(errorsOrProvider.right().get()));
            } catch (Exception e) {
                return Either.left(ErrorCollection.of("Error when saving the provider: " + e.getMessage()));
            }
        }
    }

    @Nonnull
    public Either<ErrorCollection, OpenIdProvider> validateUpdate(@Nullable OpenIdProvider provider, @Nonnull ProviderBean providerBean) throws InvocationTargetException, IllegalAccessException {
        final ProviderType providerType = providerTypeFactory.getProviderTypeById(providerBean.getProviderType());

        final Either<ErrorCollection, Map<String, Object>> errorsOrProvider = providerType.validateUpdate(provider, providerBean);
        if (errorsOrProvider.isLeft()) {
            return Either.left(errorsOrProvider.left().get());
        } else {
            BeanUtils.populate(provider, errorsOrProvider.right().get());
            provider.save();
            return Either.right(provider);
        }
    }
}

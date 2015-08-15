package com.pawelniewiadomski.jira.openid.authentication.services;

import com.atlassian.fugue.Either;
import com.atlassian.sal.api.message.I18nResolver;
import com.pawelniewiadomski.jira.openid.authentication.Errors;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.ProviderType;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ProviderValidator {

    final OpenIdDao openIdDao;

    final I18nResolver i18nResolver;

    final OpenIdDiscoveryDocumentProvider discoveryDocumentProvider;

    final ProviderTypeFactory providerTypeFactory;

    public ProviderValidator(OpenIdDao openIdDao, I18nResolver i18nResolver, OpenIdDiscoveryDocumentProvider discoveryDocumentProvider, ProviderTypeFactory providerTypeFactory) {
        this.openIdDao = openIdDao;
        this.i18nResolver = i18nResolver;
        this.discoveryDocumentProvider = discoveryDocumentProvider;
        this.providerTypeFactory = providerTypeFactory;
    }

    @Nonnull
    public Either<Errors, OpenIdProvider> validateCreate(ProviderBean providerBean) {
        final ProviderType providerType = providerTypeFactory.getProviderTypeById(providerBean.getProviderType());

        final Either<Errors, Map<String, Object>> errorsOrProvider = providerType.validateCreate(providerBean);
        if (errorsOrProvider.isLeft()) {
            return Either.left(errorsOrProvider.left().get());
        } else {
            try {
                return Either.right(openIdDao.createProvider(errorsOrProvider.right().get()));
            } catch (Exception e) {
                return Either.left(new Errors().addErrorMessage("Error when saving the provider: " + e.getMessage()));
            }
        }
    }

    @Nonnull
    public Either<Errors, OpenIdProvider> validateUpdate(@Nullable OpenIdProvider provider, @Nonnull ProviderBean providerBean) throws InvocationTargetException, IllegalAccessException {
        final ProviderType providerType = providerTypeFactory.getProviderTypeById(providerBean.getProviderType());

        final Either<Errors, Map<String, Object>> errorsOrProvider = providerType.validateUpdate(provider, providerBean);
        if (errorsOrProvider.isLeft()) {
            return Either.left(errorsOrProvider.left().get());
        } else {
            BeanUtils.populate(provider, errorsOrProvider.right().get());
            provider.save();
            return Either.right(provider);
        }
    }
}

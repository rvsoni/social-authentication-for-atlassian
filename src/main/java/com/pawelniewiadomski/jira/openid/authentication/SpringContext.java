package com.pawelniewiadomski.jira.openid.authentication;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */

import com.google.common.collect.Iterables;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static LicenseProvider getLicenseProvider() {
        ApplicationContext applicationContext = SpringContext.getApplicationContext();
        return applicationContext != null ? Iterables.<LicenseProvider>getFirst(applicationContext.getBeansOfType(LicenseProvider.class).values(), null) : null;
    }
}
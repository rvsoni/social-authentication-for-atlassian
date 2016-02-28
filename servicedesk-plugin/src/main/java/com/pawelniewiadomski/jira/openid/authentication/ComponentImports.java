package com.pawelniewiadomski.jira.openid.authentication;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class ComponentImports {
    @ComponentImport
    private ActiveObjects activeObjects;
    @ComponentImport("salUserManager")
    private UserManager userManager;
    @ComponentImport
    private CrowdService crowdService;
    @ComponentImport
    private PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private JiraPropertySetFactory jiraPropertySetFactory;
    @ComponentImport
    private SoyTemplateRenderer soyTemplateRenderer;
    @ComponentImport
    private com.atlassian.plugin.PluginAccessor pluginAccessor;
    @ComponentImport
    private com.atlassian.plugin.PluginController pluginController;
    @ComponentImport
    private com.atlassian.sal.api.transaction.TransactionTemplate transactionTemplate;
    @ComponentImport
    private com.atlassian.templaterenderer.TemplateRenderer templateRenderer;
    @ComponentImport
    private com.atlassian.sal.api.auth.LoginUriProvider loginUriProvider;
    @ComponentImport
    private com.atlassian.sal.api.message.I18nResolver i18nResolver;
    @ComponentImport
    private com.atlassian.upm.api.license.PluginLicenseManager pluginLicenseManager;
    @ComponentImport
    private com.atlassian.webresource.api.assembler.PageBuilderService pageBuilderService;
    @ComponentImport("salApplicationProperties")
    private com.atlassian.sal.api.ApplicationProperties salApplicationProperties;
    @ComponentImport("applicationProperties")
    private com.atlassian.jira.config.properties.ApplicationProperties applicationProperties;
}

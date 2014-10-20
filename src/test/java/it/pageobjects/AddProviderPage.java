package it.pageobjects;

import java.util.Map;

import com.atlassian.jira.pageobjects.form.FormUtils;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.apache.sanselan.formats.jpeg.segments.APPNSegment;
import org.openqa.selenium.By;

public class AddProviderPage extends AbstractJiraPage {
    @ElementBy(id = "name")
    PageElement name;

    @ElementBy(cssSelector = "form.aui")
    PageElement form;

    @ElementBy(id = "endpointUrl")
    PageElement endpointUrl;

    @ElementBy(id = "extensionNamespace")
    PageElement extensionNamespace;

    @ElementBy(id = "allowedDomains")
    PageElement allowedDomains;

    @ElementBy(id = "saveProvider")
    PageElement saveProvider;

    @ElementBy(id = "oauth2")
    CheckboxElement oauth2Checkbox;

    @ElementBy(id = "openid1")
    CheckboxElement openIdCheckbox;

    @ElementBy(id = "clientId")
    PageElement clientId;

    @ElementBy(id = "clientSecret")
    PageElement clientSecret;

    @ElementBy(id = "callbackId")
    WebDriverElement callbackId;

    @Override
    public TimedCondition isAt() {
        return allowedDomains.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/openid-configuration?op=add";
    }

    public AddProviderPage setName(final String name) {
        this.name.clear();
        this.name.type(name);
        return this;
    }

    public AddProviderPage setEndpointUrl(final String s) {
        this.endpointUrl.clear();
        this.endpointUrl.type(s);
        return this;
    }

    public AddProviderPage saveWithErrors() {
        saveProvider.click();
        return this;
    }

    public ConfigurationPage save() {
        saveProvider.click();
        return pageBinder.bind(ConfigurationPage.class);
    }

    public Map<String, String> getFormErrors()
    {
        return FormUtils.getAuiFormErrors(form);
    }

    public TimedQuery<String> getName() {
        return name.timed().getValue();
    }

    public TimedQuery<String> getEndpointUrl() {
        return endpointUrl.timed().getValue();
    }

    public TimedCondition isEndpointUrlVisible() {
        return endpointUrl.timed().isVisible();
    }

    public TimedCondition isNameVisible() {
        return name.timed().isVisible();
    }

    public TimedCondition isAllowedDomainsVisible() {
        return allowedDomains.timed().isVisible();
    }

    public TimedCondition isExtensionNamespaceVisible() {
        return extensionNamespace.timed().isVisible();
    }

    public AddProviderPage setExtensionNamespace(String namespace) {
        this.extensionNamespace.clear().type(namespace);
        return this;
    }

    public AddProviderPage setClientId(String clientId) {
        this.clientId.clear().type(clientId);
        return this;
    }

    public AddProviderPage setClientSecret(String clientSecret) {
        this.clientSecret.clear().type(clientSecret);
        return this;
    }

    public AddProviderPage setCallbackId(String id) {
        driver.executeScript("AJS.$('#callbackId').val(arguments[0])", id);
        return this;
    }

    public AddProviderPage setProviderType(String providerType) {
        elementFinder.find(By.id(providerType), CheckboxElement.class).check();
        return this;
    }

    public AddProviderPage setAllowedDomains(String allowedDomains) {
        this.allowedDomains.clear().type(allowedDomains);
        return this;
    }
}

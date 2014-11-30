package it.pageobjects;

import java.util.Map;

import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.pageobjects.components.IssuePickerPopup;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.elements.*;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.components.fields.IssuePickerRowMatchers.hasIssueKey;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.atlassian.pageobjects.elements.query.Conditions.forMatcher;

public class AddProviderPage extends AbstractJiraPage {
    @Inject
    protected ExtendedElementFinder extendedFinder;

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

    @ElementBy(id = "providerType")
    SelectElement providerType;

    @ElementBy(id = "callbackUrl")
    PageElement callbackUrl;

    @Override
    public TimedCondition isAt() {
        return allowedDomains.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/openid-configuration#/create";
    }

    public AddProviderPage saveWithErrors() {
        saveProvider.click();
        return this;
    }

    public ConfigurationPage save() {
        saveProvider.click();
        return pageBinder.bind(ConfigurationPage.class);
    }

    public TimedQuery<Iterable<AuiErrorMessage>> getFormErrors() {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(form).newQuery(By.cssSelector("div.error"))
                        .filter(PageElements.hasDataAttribute("field"))
                        .supplier(),
                AuiErrorMessage.class);
    }

    public TimedCondition hasFormError(@Nonnull String fieldId)
    {
        return forMatcher(getFormErrors(), IterableMatchers.hasItemThat(hasDataField(fieldId)));
    }

    public AuiErrorMessage getFormError(@Nonnull String fieldId)
    {
        Poller.waitUntilTrue(hasFormError(fieldId));
        return Iterables.find(getFormErrors().now(), GuavaPredicates.forMatcher(hasDataField(fieldId)));
    }

    public TimedQuery<String> getName() {
        return name.timed().getValue();
    }

    public AddProviderPage setName(final String name) {
        this.name.clear();
        this.name.type(name);
        return this;
    }

    public TimedQuery<String> getEndpointUrl() {
        return endpointUrl.timed().getValue();
    }

    public AddProviderPage setEndpointUrl(final String s) {
        this.endpointUrl.clear();
        this.endpointUrl.type(s);
        return this;
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

    public TimedCondition hasErrors() {
        return elementFinder.find(By.cssSelector("div.error")).timed().isPresent();
    }

    public TimedQuery<String> getCallbackUrl() {
        return callbackUrl.timed().getText();
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
        driver.executeScript("var cid = arguments[0]; angular.element('form[name=\"providerForm\"]').scope().$apply(" +
                "function($scope) { $scope.provider.callbackId = cid; });", id);
        return this;
    }

    public AddProviderPage setProviderType(String providerType) {
        this.providerType.select(Options.text(providerType));
        return this;
    }

    public AddProviderPage setAllowedDomains(String allowedDomains) {
        this.allowedDomains.clear().type(allowedDomains);
        return this;
    }

    public TimedQuery<String> getExtensionNamespace() {
        return this.extensionNamespace.timed().getValue();
    }

    public AddProviderPage setExtensionNamespace(String namespace) {
        this.extensionNamespace.clear().type(namespace);
        return this;
    }

    public TimedQuery<Boolean> isSelectProviderTypeVisible() {
        return providerType.timed().isVisible();
    }

    public TimedQuery<String> getClientSecret() {
        return clientSecret.timed().getValue();
    }

    public TimedQuery<String> getClientId() {
        return clientId.timed().getValue();
    }

    public TimedQuery<String> getAllowedDomains() {
        return allowedDomains.timed().getValue();
    }

    public static class AuiErrorMessage
    {
        protected final PageElement errorMessage;

        public AuiErrorMessage(PageElement errorMessage)
        {
            this.errorMessage = errorMessage;
        }

        public TimedQuery<String> getFieldId()
        {
            return DataAttributeFinder.query(errorMessage).timed().getDataAttribute("field");
        }

        public TimedQuery<String> getMessage()
        {
            return errorMessage.timed().getText();
        }
    }

    public static TypeSafeMatcher<AuiErrorMessage> hasDataField(final String dataField)
    {
        Assertions.notNull("dataField", dataField);
        return new TypeSafeMatcher<AuiErrorMessage>()
        {
            @Override
            public boolean matchesSafely(AuiErrorMessage issue)
            {
                return issue.getFieldId().now().matches(dataField);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Errors contains one with data-field ").appendValue(dataField);
            }
        };
    }

    public static TypeSafeMatcher<AuiErrorMessage> hasErrorMessage(final String errorMessage)
    {
        Assertions.notNull("errorMessage", errorMessage);
        return new TypeSafeMatcher<AuiErrorMessage>()
        {
            @Override
            public boolean matchesSafely(AuiErrorMessage issue)
            {
                return issue.getMessage().now().matches(errorMessage);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Errors message is ").appendValue(errorMessage);
            }
        };
    }

}

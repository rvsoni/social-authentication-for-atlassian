package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

public class ConfigurationPage extends AbstractJiraAdminPage {

    @ElementBy(id = "openid")
    PageElement openIdWrapper;

    @ElementBy(id = "creatingUsersState")
    PageElement creatingUsersState;

    @ElementBy(id = "switchCreatingUsers")
    PageElement switchCreatingUsers;

    @ElementBy(id = "advanced")
    PageElement advanced;

    @ElementBy(id = "addProvider")
    PageElement addProvider;

    @ElementBy(id = "allowedDomains")
    PageElement allowedDomains;

    @ElementBy(id = "save")
    PageElement saveAllowedDomains;

    @Override
    public String linkId() {
        return "openid";
    }

    @Override
    public TimedCondition isAt() {
        return openIdWrapper.timed().isPresent();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/openid-configuration";
    }

    public TimedCondition isAdvancedPressed() {
        return advanced.timed().hasAttribute("aria-pressed", "true");
    }

    public TimedCondition isAllowedDomainsVisible() {
        return allowedDomains.timed().isVisible();
    }

    public TimedCondition isSaveAllowedDomainsVisible() {
        return saveAllowedDomains.timed().isVisible();
    }

    public TimedCondition isCreatingUsersEnabled() {
        return creatingUsersState.timed().hasClass("aui-lozenge-success");
    }

    public ConfigurationPage clickAdvanced() {
        advanced.click();
        return this;
    }

    public ConfigurationPage setAllowedDomains(final String s) {
        allowedDomains.clear().type(s);
        return this;
    }

    public ConfigurationPage save() {
        saveAllowedDomains.click();
        return this;
    }

    public TimedQuery<String> getAllowedDomains() {
        return allowedDomains.timed().getValue();
    }

    public EditProviderPage editProvider(String name) {
        elementFinder.find(By.cssSelector(String.format("td[data-provider-name='%s'] .edit", name))).click();
        return pageBinder.bind(EditProviderPage.class);
    }
}

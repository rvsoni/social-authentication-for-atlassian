package it.common.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

public class ConfigurationPage extends AbstractJiraAdminPage {

    @ElementBy(id = "openid")
    PageElement openIdWrapper;

    @ElementBy(id = "creatingUsersState")
    PageElement creatingUsersState;

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

    public TimedCondition isCreatingUsersEnabled() {
        return creatingUsersState.timed().hasClass("aui-lozenge-success");
    }

    public EditProviderPage editProvider(String name) {
        elementFinder.find(By.cssSelector(String.format("td[data-provider-name='%s'] .edit", name))).click();
        return pageBinder.bind(EditProviderPage.class);
    }
}

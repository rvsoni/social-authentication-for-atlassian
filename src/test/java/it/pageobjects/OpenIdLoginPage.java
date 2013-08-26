package it.pageobjects;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.LoginPage;
import org.openqa.selenium.By;

public class OpenIdLoginPage extends JiraLoginPage {

    @ElementBy(id = "openid-button")
    PageElement openIdButton;

    DropDown openIdProviders;

    @Init
    public void initialise()
    {
        openIdProviders = pageBinder.bind(DropDown.class, By.cssSelector("#openid-button"), By.id("openid-providers"));
    }

    public TimedCondition isOpenIdButtonVisible() {
        return openIdButton.timed().isVisible();
    }

    public DropDown getOpenIdProviders() {
        return openIdProviders;
    }
}

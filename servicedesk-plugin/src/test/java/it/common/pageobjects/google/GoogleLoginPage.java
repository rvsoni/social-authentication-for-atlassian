package it.common.pageobjects.google;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class GoogleLoginPage extends AbstractJiraPage {
    @ElementBy(id = "Email")
    PageElement email;

    @ElementBy(id = "next")
    PageElement nextButton;

    @ElementBy(id = "account-chooser-link")
    PageElement accountChooserLink;

    @Override
    public TimedCondition isAt() {
        return email.timed().isVisible();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isEmailVisible() {
        return this.email.isVisible();
    }

    public GoogleLoginPage setEmail(String email) {
        this.email.clear();
        this.email.type(email);
        return this;
    }

    public GooglePasswordPage next() {
        this.nextButton.click();
        return pageBinder.bind(GooglePasswordPage.class);
    }

    public GoogleAccountChooserPage selectDifferentAccount() {
        accountChooserLink.click();
        return pageBinder.bind(GoogleAccountChooserPage.class);
    }
}

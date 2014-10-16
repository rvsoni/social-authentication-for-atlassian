package it.pageobjects.google;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class GoogleAccountChooserPage extends AbstractJiraPage {
    @ElementBy(id = "account-chooser-add-account")
    PageElement addAccountLink;

    @Override
    public TimedCondition isAt() {
        return addAccountLink.timed().isVisible();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public GoogleLoginPage addAccount() {
        addAccountLink.click();
        return pageBinder.bind(GoogleLoginPage.class);
    }
}

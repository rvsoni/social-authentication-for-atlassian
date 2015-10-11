package it.common.pageobjects.google;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class GooglePasswordPage extends AbstractJiraPage {
    @ElementBy(id = "Passwd")
    PageElement password;

    @ElementBy(id = "signIn")
    PageElement signInButton;

    @Override
    public TimedCondition isAt() {
        return password.timed().isVisible();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public GooglePasswordPage setPassword(String password) {
        this.password.clear();
        this.password.type(password);
        return this;
    }

    public TimedCondition isSignInEnabled()
    {
        return signInButton.timed().isEnabled();
    }

    public com.atlassian.pageobjects.DelayedBinder<GoogleApprovePage> signIn() {
        this.signInButton.click();
        return pageBinder.delayedBind(GoogleApprovePage.class);
    }
}

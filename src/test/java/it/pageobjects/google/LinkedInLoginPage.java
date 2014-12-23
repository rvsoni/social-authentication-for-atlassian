package it.pageobjects.google;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class LinkedInLoginPage extends AbstractJiraPage {
    @ElementBy(name = "session_key")
    PageElement email;

    @ElementBy(name = "session_password")
    PageElement password;

    @ElementBy(name = "authorize")
    PageElement signInButton;

    @Override
    public TimedCondition isAt() {
        return password.timed().isVisible();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isEmailVisible() {
        return this.email.isVisible();
    }

    public LinkedInLoginPage setEmail(String email) {
        this.email.clear();
        this.email.type(email);
        return this;
    }

    public LinkedInLoginPage setPassword(String password) {
        this.password.clear();
        this.password.type(password);
        return this;
    }

    public TimedCondition isSignInEnabled()
    {
        return signInButton.timed().isEnabled();
    }

    public com.atlassian.pageobjects.DelayedBinder<LinkedInApprovePage> signIn() {
        this.signInButton.click();
        return pageBinder.delayedBind(LinkedInApprovePage.class);
    }
}

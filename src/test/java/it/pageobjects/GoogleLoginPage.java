package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class GoogleLoginPage extends AbstractJiraPage {
    @ElementBy(id = "Email")
    PageElement email;

    @ElementBy(id = "Passwd")
    PageElement password;

    @ElementBy(id = "signIn")
    PageElement signInButton;

    @ElementBy(id = "PersistentCookie")
    CheckboxElement persistentCookie;

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

    public GoogleLoginPage setEmail(String email) {
        this.email.clear();
        this.email.type(email);
        return this;
    }

    public GoogleLoginPage setPassword(String password) {
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

    public void setPersistentCookie(final boolean b) {
        if (b) {
            persistentCookie.check();
        } else {
            persistentCookie.uncheck();
        }
    }
}

package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
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

    @Override
    public TimedCondition isAt() {
        return email.timed().isVisible();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
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

    public void signIn() {
        this.signInButton.click();
    }
}

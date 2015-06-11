package it.jira.pageobjects.google;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class GoogleApprovePage extends AbstractJiraPage {
    @ElementBy(id = "submit_approve_access")
    PageElement approveButton;

    @Override
    public TimedCondition isAt() {
        return approveButton.timed().isEnabled();
    }

    public void approve()
    {
        approveButton.click();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }
}

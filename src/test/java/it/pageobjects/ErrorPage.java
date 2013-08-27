package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public class ErrorPage extends AbstractJiraPage {
    @ElementBy(className = "error-message")
    PageElement errorMessage;

    @Override
    public TimedCondition isAt() {
        return elementFinder.find(By.cssSelector(".openid.error")).timed().isPresent();
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public TimedQuery<String> getErrorMessage() {
        return errorMessage.timed().getText();
    }
}

package it.jira;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.query.Poller;
import it.common.AddProviderAssertions;
import it.jira.pageobjects.AddProviderPage;
import it.jira.pageobjects.ConfigurationPage;
import it.jira.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static it.jira.pageobjects.AddProviderPage.hasErrorMessage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class TestAddProvider extends BaseJiraWebTest {

    private AddProviderPage addPage;
    private AddProviderAssertions addProviderAssertions;

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().project().addProject("Test", "TST", "admin");

        addPage = jira.visit(AddProviderPage.class);
        addProviderAssertions = new AddProviderAssertions(addPage);
    }

    @Test
    public void testAddOAuthErrors() {
        addProviderAssertions.testAddOAuthErrors();
    }

    @Test
    public void testAddAndEdit() {
        addProviderAssertions.testAddAndEdit();
    }
}

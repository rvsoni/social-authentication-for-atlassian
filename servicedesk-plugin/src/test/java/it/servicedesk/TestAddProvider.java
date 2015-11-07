package it.servicedesk;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import it.common.AddProviderAssertions;
import it.servicedesk.pageobjects.AddProviderPage;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
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

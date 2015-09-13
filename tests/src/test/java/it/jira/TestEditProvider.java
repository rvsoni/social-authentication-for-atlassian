package it.jira;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import it.common.EditProviderAssertions;
import it.jira.pageobjects.AddProviderPage;
import it.jira.pageobjects.ConfigurationPage;
import it.jira.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static it.jira.pageobjects.AddProviderPage.hasErrorMessage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestEditProvider extends BaseJiraWebTest {

    private AddProviderPage addPage;
    private EditProviderAssertions editProviderAssertions;

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().project().addProject("Test", "TST", "admin");
        addPage = jira.visit(AddProviderPage.class);
        editProviderAssertions = new EditProviderAssertions(addPage);
    }

    @Test
    public void editOpenIdProvider()
    {
        editProviderAssertions.editOpenIdProvider();
    }

    @Test
    public void testEditOpenIdProvider() {
        editProviderAssertions.testEditOpenIdProvider();
    }

    @Test
    public void testEditOpenIdConnectProvider() {
        editProviderAssertions.testEditOpenIdConnectProvider();
    }

    @Test
    public void testEditGoogleProvider() {
        editProviderAssertions.testEditGoogleProvider();
    }
}

package it;

import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.elements.query.Poller;
import it.pageobjects.AddProviderPage;
import it.pageobjects.ConfigurationPage;
import it.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public class TestAddProvider extends TestBase {

    private AddProviderPage addPage;

    @Before
    public void setUp() {
        jira().backdoor().restoreBlankInstance();
        jira().backdoor().project().addProject("Test", "TST", "admin");

        addPage = jira().gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
    }

    @Test
    public void testAddAndEdit() {
        final String name = "Testing";
        final String endpointUrl = "http://asdkasjdkald.pl";

        addPage.setName(name);
        addPage.setEndpointUrl(endpointUrl);

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Testing");
        Poller.waitUntil(editPage.getName(), (Matcher<String>) equalTo(name));
        Poller.waitUntil(editPage.getEndpointUrl(), (Matcher<String>) equalTo(endpointUrl));
        editPage.save();
    }
}

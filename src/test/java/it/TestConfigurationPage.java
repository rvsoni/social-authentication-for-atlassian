package it;

import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.elements.query.Poller;
import it.pageobjects.ConfigurationPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class TestConfigurationPage extends TestBase {

    private ConfigurationPage configuration;

    @Before
    public void setUp() {
        jira().backdoor().restoreBlankInstance();
        jira().backdoor().project().addProject("Test", "TST", "admin");

        configuration = jira().gotoLoginPage().loginAsSysAdmin(ConfigurationPage.class);
    }

    @Test
    public void testSimpleModeIsDefault() {
        Poller.waitUntilFalse(configuration.isAdvancedPressed());
        Poller.waitUntilTrue(configuration.isAllowedDomainsVisible());
        Poller.waitUntilTrue(configuration.isSaveAllowedDomainsVisible());
        Poller.waitUntilFalse(configuration.isCreatingUsersEnabled());
    }

    @Test
    public void testSaveAllowedDomains() {
        configuration.setAllowedDomains("test.pl, google.com").save();
        Poller.waitUntil(configuration.getAllowedDomains(), (Matcher<String>) equalTo("test.pl, google.com"));
    }

}

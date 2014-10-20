package it;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.pageobjects.elements.query.Poller;
import it.pageobjects.AddProviderPage;
import it.pageobjects.ConfigurationPage;
import it.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestAddProvider extends BaseJiraWebTest {

    private AddProviderPage addPage;

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().project().addProject("Test", "TST", "admin");

        addPage = jira.gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
    }

    @Test
    @LoginAs(anonymous = true)
    public void testAddAndEdit() {
        final String name = "Testing";
        final String endpointUrl = "http://asdkasjdkald.pl";

        addPage.setName(name);
        addPage.setEndpointUrl(endpointUrl);
        addPage.setExtensionNamespace("ext1");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Testing");
        Poller.waitUntil(editPage.getName(), (Matcher<String>) equalTo(name));
        Poller.waitUntil(editPage.getEndpointUrl(), (Matcher<String>) equalTo(endpointUrl));

        editPage.setName("");
        editPage.setEndpointUrl("");
        editPage.save();

        assertThat(editPage.getFormErrors(), hasEntry("name", "Name must not be empty."));
        assertThat(editPage.getFormErrors(), hasEntry("endpointUrl", "Provider URL must not be empty."));
    }
}

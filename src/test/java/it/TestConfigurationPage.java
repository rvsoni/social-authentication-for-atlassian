package it;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.testkit.client.model.JiraMode;
import it.pageobjects.ConfigurationPage;
import org.junit.Before;

public class TestConfigurationPage extends BaseJiraWebTest {

    private ConfigurationPage configuration;

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        backdoor.generalConfiguration().setJiraMode(JiraMode.PRIVATE);
        jira.backdoor().project().addProject("Test", "TST", "admin");

        configuration = jira.gotoLoginPage().loginAsSysAdmin(ConfigurationPage.class);
    }
}

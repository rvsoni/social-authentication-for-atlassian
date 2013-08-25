package it;

import com.atlassian.jira.tests.TestBase;
import it.pageobjects.ConfigurationPage;
import org.junit.Test;

public class TestConfigurationPage extends TestBase {

    @Test
    public void testSimpleModeIsDefault() {
        ConfigurationPage configuration = jira().gotoLoginPage().loginAsSysAdmin(ConfigurationPage.class);
    }

}

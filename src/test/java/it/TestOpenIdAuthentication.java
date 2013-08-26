package it;

import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.elements.query.Poller;
import it.pageobjects.ConfigurationPage;
import it.pageobjects.OpenIdLoginPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

public class TestOpenIdAuthentication extends TestBase {

    ConfigurationPage configuration;
    final Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @Before
    public void setUp() {
        jira().backdoor().restoreBlankInstance();
        jira().backdoor().project().addProject("Test", "TST", "admin");

        configuration = jira().gotoLoginPage().loginAsSysAdmin(ConfigurationPage.class);
        configuration.setAllowedDomains("spartez.com").save();
        jira().logout();

        OpenIdLoginPage loginPage = jira().visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));
    }

    @Test
    public void testLogInWithinAllowedDomainsWork() {

    }

    @Test
    public void testLogInOutsideAllowedDomainsIsProhibited() {

    }
}

package it;

import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.elements.query.Poller;
import it.pageobjects.ConfigurationPage;
import it.pageobjects.ErrorPage;
import it.pageobjects.GoogleLoginPage;
import it.pageobjects.OpenIdLoginPage;
import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

public class TestOpenIdAuthentication extends TestBase {

    final Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @BeforeClass
    public static void setUp() throws JSONException {
        jira().backdoor().restoreBlankInstance();
        jira().backdoor().plugins().setPluginLicense(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);
        jira().backdoor().project().addProject("Test", "TST", "admin");

        ConfigurationPage configuration = jira().gotoLoginPage().loginAsSysAdmin(ConfigurationPage.class);
        configuration.setAllowedDomains("spartez.com").save();
    }

    @After
    public void tearDown() {
        jira().logout();
        jira().getTester().getDriver().navigate().to("https://accounts.google.com/Logout?hl=en&amp;continue=https://www.google.pl/%3Fgws_rd%3Dcr%26ei%3DmzUcUr2UD4zItAaZvoHoBA");
    }

    @Test
    public void testLogInWithinAllowedDomainsWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OpenIdLoginPage loginPage = jira().visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        GoogleLoginPage googleLoginPage = jira().getPageBinder().bind(GoogleLoginPage.class);
        googleLoginPage.setEmail((String) PropertyUtils.getProperty(passwords, "spartez.user"));
        googleLoginPage.setPassword((String) PropertyUtils.getProperty(passwords, "spartez.password"));
        googleLoginPage.signIn();

        jira().getPageBinder().bind(DashboardPage.class);
    }

    @Test
    public void testLogInOutsideAllowedDomainsIsProhibited() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OpenIdLoginPage loginPage = jira().visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        GoogleLoginPage googleLoginPage = jira().getPageBinder().bind(GoogleLoginPage.class);
        googleLoginPage.setEmail((String) PropertyUtils.getProperty(passwords, "gmail.user"));
        googleLoginPage.setPassword((String) PropertyUtils.getProperty(passwords, "gmail.password"));
        googleLoginPage.signIn();

        ErrorPage errorPage = jira().getPageBinder().bind(ErrorPage.class);
        Poller.waitUntil(errorPage.getErrorMessage(), containsString("allowed domains"));
    }
}

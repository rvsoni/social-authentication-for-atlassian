package it;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.testing.annotation.TestedProductClass;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import it.pageobjects.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.hamcrest.CoreMatchers.containsString;

public class TestOpenIdAuthentication extends BaseJiraWebTest {

    final static Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @BeforeClass
    public static void setUp() throws JSONException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().plugins().setPluginLicense(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);
        jira.backdoor().project().addProject("Test", "TST", "admin");

        AddProviderPage addProvider = jira.gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
        addProvider.setProviderType(OpenIdProvider.OAUTH2_TYPE)
                .setName("Google")
                .setEndpointUrl("https://accounts.google.com")
                .setCallbackId((String) getProperty(passwords, "google.callbackId"))
                .setClientId((String) getProperty(passwords, "google.clientId"))
                .setClientSecret((String) getProperty(passwords, "google.clientSecret"))
                .setAllowedDomains("test.pl, teamstatus.tv, abc.pl").save();

        jira.getTester().getDriver().manage().deleteAllCookies();
    }

    @After
    public void tearDown() {
        jira.getTester().getDriver().manage().deleteAllCookies();
        jira.getTester().getDriver().navigate().to("https://accounts.google.com/Logout?hl=en&continue=https://www.google.pl/");
    }

    @Test
    public void testLogInWithinAllowedDomainsWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OpenIdLoginPage loginPage = jira.visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        GoogleLoginPage googleLoginPage = jira.getPageBinder().bind(GoogleLoginPage.class);
        googleLoginPage.setPersistentCookie(false);
        googleLoginPage.setEmail((String) getProperty(passwords, "teamstatus.user"));
        googleLoginPage.setPassword((String) getProperty(passwords, "teamstatus.password"));

        Poller.waitUntilTrue(googleLoginPage.isSignInEnabled());
        DelayedBinder<GoogleApprovePage> approvePage = googleLoginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }
        jira.getPageBinder().bind(DashboardPage.class);
    }

    @Test
    public void testLogInOutsideAllowedDomainsIsProhibited() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OpenIdLoginPage loginPage = jira.visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        GoogleLoginPage googleLoginPage = jira.getPageBinder().bind(GoogleLoginPage.class);
        googleLoginPage.setPersistentCookie(false);
        googleLoginPage.setEmail((String) getProperty(passwords, "gmail.user"));
        googleLoginPage.setPassword((String) getProperty(passwords, "gmail.password"));
        DelayedBinder<GoogleApprovePage> approvePage = googleLoginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }

        ErrorPage errorPage = jira.getPageBinder().bind(ErrorPage.class);
        Poller.waitUntil(errorPage.getErrorMessage(), containsString("allowed domains"));
    }

    @Test
    public void testLogInRedirectsToReturnUrl() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.getTester().getDriver().navigate().to(jira.getProductInstance().getBaseUrl()
                + "/login.jsp?os_destination=%2Fsecure%2Fproject%2FViewProjects.jspa");
        OpenIdLoginPage loginPage = jira.getPageBinder().bind(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        GoogleLoginPage googleLoginPage = jira.getPageBinder().bind(GoogleLoginPage.class);
        googleLoginPage.setPersistentCookie(false);
        googleLoginPage.setEmail((String) getProperty(passwords, "teamstatus.user"));
        googleLoginPage.setPassword((String) getProperty(passwords, "teamstatus.password"));

        Poller.waitUntilTrue(googleLoginPage.isSignInEnabled());
        DelayedBinder<GoogleApprovePage> approvePage = googleLoginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }
        jira.getPageBinder().bind(ViewProjectsPage.class);
    }
}

package it;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.common.base.Preconditions;
import it.pageobjects.AddProviderPage;
import it.pageobjects.OpenIdLoginPage;
import it.pageobjects.google.GithubApprovePage;
import it.pageobjects.google.GithubLoginPage;
import it.pageobjects.google.LinkedInApprovePage;
import it.pageobjects.google.LinkedInLoginPage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.apache.commons.beanutils.PropertyUtils.getProperty;

public class TestLinkedInAuthentication extends BaseJiraWebTest {

    final static Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @BeforeClass
    public static void setUp() throws JSONException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().plugins().setPluginLicense(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);
        jira.backdoor().project().addProject("Test", "TST", "admin");

        AddProviderPage addProvider = jira.gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
        addProvider.setProviderType("LinkedIn")
                .setClientId((String) getProperty(passwords, "linkedin.clientId"))
                .setClientSecret((String) getProperty(passwords, "linkedin.clientSecret"))
                .save();

        jira.getTester().getDriver().manage().deleteAllCookies();
    }

    @After
    public void tearDown() {
        jira.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    @LoginAs(anonymous = true)
    public void testLogInWorks() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        OpenIdLoginPage loginPage = jira.visit(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        loginDance((String) getProperty(passwords, "linkedin.user"), (String) getProperty(passwords, "linkedin.password"));

        jira.getPageBinder().bind(DashboardPage.class);
    }

    protected void loginDance(String email, String password)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Preconditions.checkNotNull(email);
        Preconditions.checkNotNull(password);

        LinkedInLoginPage loginPage = jira.getPageBinder().bind(LinkedInLoginPage.class);

        loginPage.setEmail(email);
        loginPage.setPassword(password);

        Poller.waitUntilTrue(loginPage.isSignInEnabled());
        DelayedBinder<LinkedInApprovePage> approvePage = loginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }
    }

    @Test
    @LoginAs(anonymous = true)
    public void testLogInRedirectsToReturnUrl() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.getTester().getDriver().navigate().to(jira.getProductInstance().getBaseUrl()
                + "/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa");
        OpenIdLoginPage loginPage = jira.getPageBinder().bind(OpenIdLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.getOpenIdProviders().openAndClick(By.id("openid-1"));

        loginDance((String) getProperty(passwords, "linkedin.user"), (String) getProperty(passwords, "linkedin.password"));

        jira.getPageBinder().bind(ViewProfilePage.class);
    }
}

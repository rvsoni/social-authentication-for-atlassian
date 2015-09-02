package it.jira;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.common.base.Preconditions;
import it.common.ItEnvironment;
import it.common.pageobjects.google.GithubApprovePage;
import it.common.pageobjects.google.GithubLoginPage;
import it.jira.pageobjects.AddProviderPage;
import it.jira.pageobjects.JiraLoginPage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.apache.commons.beanutils.PropertyUtils.getProperty;

public class TestGithubAuthentication extends BaseJiraWebTest {

    final static Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @BeforeClass
    public static void setUp() throws JSONException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().plugins().setPluginLicense(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);
        jira.backdoor().project().addProject("Test", "TST", "admin");

        AddProviderPage addProvider = jira.gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
        addProvider.setProviderType("Github")
                .setClientId((String) getProperty(passwords, "github.clientId"))
                .setClientSecret((String) getProperty(passwords, "github.clientSecret"))
                .save();

        jira.getTester().getDriver().manage().deleteAllCookies();
    }

    @After
    public void tearDown() {
        jira.getTester().getDriver().manage().deleteAllCookies();
        jira.getTester().getDriver().navigate().to("https://github.com");
        jira.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    @LoginAs(anonymous = true)
    public void testLogInWorks() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JiraLoginPage loginPage = jira.visit(JiraLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.startAuthenticationDanceFor("GitHub");

        loginDance((String) getProperty(passwords, "github.user"), (String) getProperty(passwords, "github.password"));

        jira.getPageBinder().bind(DashboardPage.class);
    }

    protected void loginDance(String email, String password)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Preconditions.checkNotNull(email);
        Preconditions.checkNotNull(password);

        GithubLoginPage loginPage = jira.getPageBinder().bind(GithubLoginPage.class);

        loginPage.setEmail(email);
        loginPage.setPassword(password);

        Poller.waitUntilTrue(loginPage.isSignInEnabled());
        DelayedBinder<GithubApprovePage> approvePage = loginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }
    }

    @Test
    @LoginAs(anonymous = true)
    public void testLogInRedirectsToReturnUrl() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        jira.getTester().getDriver().navigate().to(jira.getProductInstance().getBaseUrl()
                + "/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa");
        JiraLoginPage loginPage = jira.getPageBinder().bind(JiraLoginPage.class);
        Poller.waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.startAuthenticationDanceFor("GitHub");

        loginDance((String) getProperty(passwords, "github.user"), (String) getProperty(passwords, "github.password"));

        jira.getPageBinder().bind(ViewProfilePage.class);
    }
}

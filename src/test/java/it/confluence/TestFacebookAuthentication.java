package it.confluence;

import com.atlassian.confluence.webdriver.AbstractInjectableWebDriverTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import it.common.ItEnvironment;
import it.common.pageobjects.google.FacebookApprovePage;
import it.common.pageobjects.google.FacebookLoginPage;
import it.confluence.pageobjects.ConfluenceLoginPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;

public class TestFacebookAuthentication extends AbstractInjectableWebDriverTest {

    final static Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @Before
    public void setUp() throws JSONException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        new LicenseControl(product.getProductInstance()).setPluginLicense(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);

//        AddProviderPage addProvider = product.gotoLoginPage().loginAsSysAdmin(AddProviderPage.class);
//        addProvider.setProviderType("Facebook")
//                .setClientId((String) getProperty(passwords, "facebook.confluence.clientId"))
//                .setClientSecret((String) getProperty(passwords, "facebook.confluence.clientSecret"))
//                .save();
//
//        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @After
    public void tearDown() {
        product.getTester().getDriver().manage().deleteAllCookies();
        product.getTester().getDriver().navigate().to("https://www.facebook.com");
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    public void testLogInWorks() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConfluenceLoginPage loginPage = product.visit(ConfluenceLoginPage.class);
        waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.startAuthenticationDanceFor("Facebook");

        loginDance((String) getProperty(passwords, "facebook.user"), (String) getProperty(passwords, "facebook.password"));

        product.getPageBinder().bind(DashboardPage.class);
    }

    protected void loginDance(String email, String password)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        checkNotNull(email);
        checkNotNull(password);

        FacebookLoginPage loginPage = product.getPageBinder().bind(FacebookLoginPage.class);

        loginPage.setEmail(email);
        loginPage.setPassword(password);

        waitUntilTrue(loginPage.isSignInEnabled());
        DelayedBinder<FacebookApprovePage> approvePage = loginPage.signIn();
        if (approvePage.canBind()) {
            approvePage.bind().approve();
        }
    }

    @Test
    public void testLogInRedirectsToReturnUrl() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        product.getTester().getDriver().navigate().to(product.getProductInstance().getBaseUrl()
                + "/login.action?os_destination=%2Fsecure%2FViewProfile.jspa");
        ConfluenceLoginPage loginPage = product.getPageBinder().bind(ConfluenceLoginPage.class);
        waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.startAuthenticationDanceFor("Facebook");

        loginDance((String) getProperty(passwords, "facebook.user"), (String) getProperty(passwords, "facebook.password"));

        product.getPageBinder().bind(ViewProfilePage.class);
    }
}

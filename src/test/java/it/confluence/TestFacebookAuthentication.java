package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.confluence.pageobjects.page.user.ViewUserProfilePage;
import com.atlassian.confluence.webdriver.AbstractInjectableWebDriverTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import it.common.ItEnvironment;
import it.common.PluginsRestClient;
import it.common.ProvidersRestClient;
import it.common.pageobjects.google.FacebookApprovePage;
import it.common.pageobjects.google.FacebookLoginPage;
import it.confluence.pageobjects.ConfluenceLoginPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;

public class TestFacebookAuthentication extends AbstractInjectableWebDriverTest {

    final static Map<String, Object> passwords = ItEnvironment.getConfiguration();

    @Before
    public void setUp() throws JSONException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ProductInstance productInstance = product.getProductInstance();

        new PluginsRestClient(productInstance).setPluginLicenseIfInvalid(ItEnvironment.PLUGIN_KEY, ItEnvironment.LICENSE_3HR);

        final ProvidersRestClient providersRestClient = new ProvidersRestClient(productInstance);
        Optional<ProviderBean> providers = providersRestClient.getProviders().stream()
                .filter(p -> p.getName().equals("Facebook")).findFirst();
        if (!providers.isPresent()) {
            providersRestClient.createProvider(ProviderBean.builder()
                    .clientId((String) getProperty(passwords, "facebook.confluence.clientId"))
                    .clientSecret((String) getProperty(passwords, "facebook.confluence.clientSecret"))
                    .providerType("facebook").build());
        }
        product.getTester().getDriver().manage().deleteAllCookies();
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
                + "/login.action?os_destination=%2Fusers%2Fviewmyprofile.action&permissionViolation=true");
        ConfluenceLoginPage loginPage = product.getPageBinder().bind(ConfluenceLoginPage.class);
        waitUntilTrue(loginPage.isOpenIdButtonVisible());
        loginPage.startAuthenticationDanceFor("Facebook");

        loginDance((String) getProperty(passwords, "facebook.user"), (String) getProperty(passwords, "facebook.password"));

        product.getPageBinder().bind(ViewUserProfilePage.class);
    }
}

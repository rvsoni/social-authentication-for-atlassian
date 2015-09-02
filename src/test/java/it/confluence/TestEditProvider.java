package it.confluence;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.webdriver.AbstractInjectableWebDriverTest;
import it.common.EditProviderAssertions;
import it.jira.pageobjects.AddProviderPage;
import org.junit.Before;
import org.junit.Test;

public class TestEditProvider extends AbstractInjectableWebDriverTest {

    private AddProviderPage addPage;
    private EditProviderAssertions editProviderAssertions;

    @Before
    public void setUp() {
        addPage = product.login(User.ADMIN, AddProviderPage.class);
        editProviderAssertions = new EditProviderAssertions(addPage);
    }

    @Test
    public void editOpenIdProvider()
    {
        editProviderAssertions.editOpenIdProvider();
    }

    @Test
    public void testEditOpenIdProvider() {
        editProviderAssertions.testEditOpenIdProvider();
    }

    @Test
    public void testEditOpenIdConnectProvider() {
        editProviderAssertions.testEditOpenIdConnectProvider();
    }

    @Test
    public void testEditGoogleProvider() {
        editProviderAssertions.testEditGoogleProvider();
    }
}

package it.confluence;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.webdriver.AbstractInjectableWebDriverTest;
import it.common.AddProviderAssertions;
import it.jira.pageobjects.AddProviderPage;
import org.junit.Before;
import org.junit.Test;

public class TestAddProvider extends AbstractInjectableWebDriverTest {

    private AddProviderPage addPage;
    private AddProviderAssertions addProviderAssertions;

    @Before
    public void setUp() {
        addPage = product.login(User.ADMIN, AddProviderPage.class);
        addProviderAssertions = new AddProviderAssertions(addPage);
    }

    @Test
    public void testAddOAuthErrors() {
        addProviderAssertions.testAddOAuthErrors();
    }

    @Test
    public void testAddAndEdit() {
        addProviderAssertions.testAddAndEdit();
    }
}

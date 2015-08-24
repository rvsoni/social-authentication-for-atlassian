package it.jira;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import it.jira.pageobjects.AddProviderPage;
import it.jira.pageobjects.ConfigurationPage;
import it.jira.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static it.jira.pageobjects.AddProviderPage.hasErrorMessage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestEditProvider extends BaseJiraWebTest {

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().project().addProject("Test", "TST", "admin");
    }

    @Test
    public void editOpenIdProvider()
    {
        final String name = "Testing";
        final String endpointUrl = "http://asdkasjdkald.pl";

        AddProviderPage addPage = jira.visit(AddProviderPage.class);

        addPage.setProviderType("OpenID 1.0");
        addPage.setName(name);
        addPage.setEndpointUrl(endpointUrl);
        addPage.setExtensionNamespace("ext1");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Testing");
        waitUntil(editPage.getName(), equalTo(name));
        waitUntil(editPage.getEndpointUrl(), equalTo(endpointUrl));

        editPage.setName("");
        editPage.setEndpointUrl("");
        editPage.setExtensionNamespace("");

        assertThat(editPage.getFormError("name"), hasErrorMessage("Please provide the name."));
        assertThat(editPage.getFormError("endpointUrl"), hasErrorMessage("Please provide the provider URL."));
        assertThat(editPage.getFormError("extensionNamespace"), hasErrorMessage("Please provide the alias."));
    }

    @Test
    public void testEditOpenIdProvider() {
        final String name = "Testing";
        final String endpointUrl = "http://asdkasjdkald.pl";

        AddProviderPage addPage = jira.visit(AddProviderPage.class);

        addPage.setProviderType("OpenID 1.0");
        addPage.setName(name);
        addPage.setEndpointUrl(endpointUrl);
        addPage.setExtensionNamespace("ext1");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Testing");
        waitUntil(editPage.getName(), equalTo(name));
        waitUntil(editPage.getEndpointUrl(), equalTo(endpointUrl));

        configurationPage = editPage.setName("New Name").setEndpointUrl("http://wp.pl").setExtensionNamespace("testing").save();
        editPage = configurationPage.editProvider("New Name");
        waitUntil(editPage.getName(), equalTo("New Name"));
        waitUntil(editPage.getEndpointUrl(), equalTo("http://wp.pl"));
        waitUntil(editPage.getExtensionNamespace(), equalTo("testing"));
    }

    @Test
    public void testEditOpenIdConnectProvider() {
        AddProviderPage addPage = jira.visit(AddProviderPage.class);

        addPage.setProviderType("OpenID Connect/OAuth 2.0")
                .setName("OAuth")
                .setEndpointUrl("https://accounts.google.com")
                .setClientId("AAA")
                .setClientSecret("BBB");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("OAuth");
        waitUntil(editPage.getName(), equalTo("OAuth"));
        waitUntil(editPage.getEndpointUrl(), equalTo("https://accounts.google.com"));
        waitUntil(editPage.getClientId(), equalTo("AAA"));
        waitUntil(editPage.getClientSecret(), equalTo("BBB"));

        configurationPage = editPage.setName("New Name")
                .setClientId("NNN")
                .setClientSecret("GGG")
                .save();
        editPage = configurationPage.editProvider("New Name");
        waitUntil(editPage.getName(), equalTo("New Name"));
        waitUntil(editPage.getEndpointUrl(), equalTo("https://accounts.google.com"));
        waitUntil(editPage.getClientId(), equalTo("NNN"));
        waitUntil(editPage.getClientSecret(), equalTo("GGG"));
    }

    @Test
    public void testEditGoogleProvider() {
        AddProviderPage addPage = jira.visit(AddProviderPage.class);

        addPage.setProviderType("Google Apps")
                .setClientId("AAA")
                .setClientSecret("BBB");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Google");
        waitUntilFalse(editPage.isEndpointUrlVisible());
        waitUntilTrue(editPage.isAllowedDomainsVisible());
        waitUntil(editPage.getClientId(), equalTo("AAA"));
        waitUntil(editPage.getClientSecret(), equalTo("BBB"));

        configurationPage = editPage
                .setClientId("NNN")
                .setClientSecret("GGG")
                .setAllowedDomains("test.pl")
                .save();

        editPage = configurationPage.editProvider("Google");
        waitUntil(editPage.getClientId(), equalTo("NNN"));
        waitUntil(editPage.getClientSecret(), equalTo("GGG"));
        waitUntil(editPage.getAllowedDomains(), equalTo("test.pl"));
    }
}

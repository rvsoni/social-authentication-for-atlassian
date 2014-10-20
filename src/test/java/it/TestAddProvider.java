package it;

import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.pageobjects.elements.query.Poller;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import it.pageobjects.AddProviderPage;
import it.pageobjects.ConfigurationPage;
import it.pageobjects.EditProviderPage;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestAddProvider extends BaseJiraWebTest {

    private AddProviderPage addPage;

    @Before
    public void setUp() {
        jira.backdoor().restoreBlankInstance();
        jira.backdoor().project().addProject("Test", "TST", "admin");

        addPage = jira.visit(AddProviderPage.class);
    }

    @Test
    public void testAddOAuthErrors() {
        addPage.setProviderType(OpenIdProvider.OAUTH2_TYPE);
        addPage = addPage.saveWithErrors();

        assertThat(addPage.getFormErrors(), hasEntry("name", "Name must not be empty."));
        assertThat(addPage.getFormErrors(), hasEntry("endpointUrl", "Provider URL must not be empty."));
        assertThat(addPage.getFormErrors(), hasEntry("clientId", "Client ID must not be empty."));
        assertThat(addPage.getFormErrors(), hasEntry("clientSecret", "Client Secret must not be empty."));

        addPage.setEndpointUrl("https://accounts.google.com");
        addPage = addPage.saveWithErrors();

        assertThat(addPage.getFormErrors().size(), Matchers.equalTo(3));
        assertThat(addPage.getFormErrors(), hasEntry("name", "Name must not be empty."));
        assertThat(addPage.getFormErrors(), hasEntry("clientId", "Client ID must not be empty."));
        assertThat(addPage.getFormErrors(), hasEntry("clientSecret", "Client Secret must not be empty."));

        addPage.setEndpointUrl("https://wp.pl");
        addPage.setName("Testing").setClientId("XXX").setClientSecret("XXX");
        addPage = addPage.saveWithErrors();

        assertThat(addPage.getFormErrors().size(), Matchers.equalTo(1));
        assertThat(addPage.getFormErrors(), hasEntry("endpointUrl", "OpenId Connect discovery document at https://wp.pl/.well-known/openid-configuration is invalid or missing."));
    }

    @Test
    public void testAddAndEdit() {
        final String name = "Testing";
        final String endpointUrl = "http://asdkasjdkald.pl";

        addPage.setName(name);
        addPage.setEndpointUrl(endpointUrl);
        addPage.setExtensionNamespace("ext1");

        ConfigurationPage configurationPage = addPage.save();

        EditProviderPage editPage = configurationPage.editProvider("Testing");
        Poller.waitUntil(editPage.getName(), (Matcher<String>) equalTo(name));
        Poller.waitUntil(editPage.getEndpointUrl(), (Matcher<String>) equalTo(endpointUrl));

        editPage.setName("");
        editPage.setEndpointUrl("");
        editPage = editPage.saveWithErrors();

        assertThat(editPage.getFormErrors(), hasEntry("name", "Name must not be empty."));
        assertThat(editPage.getFormErrors(), hasEntry("endpointUrl", "Provider URL must not be empty."));
    }
}

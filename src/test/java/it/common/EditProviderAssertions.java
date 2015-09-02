package it.common;

import it.jira.pageobjects.AddProviderPage;
import it.jira.pageobjects.ConfigurationPage;
import it.jira.pageobjects.EditProviderPage;
import lombok.AllArgsConstructor;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.*;
import static it.jira.pageobjects.AddProviderPage.hasErrorMessage;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@AllArgsConstructor
public class EditProviderAssertions {
    AddProviderPage addPage;

    @Test
    public void editOpenIdProvider()
    {
        final String name = "Testing " + randomUUID();
        final String endpointUrl = "http://asdkasjdkald.pl";

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
        final String name = "Testing " + randomUUID();
        final String endpointUrl = "http://asdkasjdkald.pl";

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

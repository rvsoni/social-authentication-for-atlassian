package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

public class AddProviderPage extends AbstractJiraPage {
    @ElementBy(id = "name")
    PageElement name;

    @ElementBy(id = "endpointUrl")
    PageElement endpointUrl;

    @ElementBy(id = "extensionNamespace")
    PageElement extensionNamespace;

    @ElementBy(id = "allowedDomains")
    PageElement allowedDomains;

    @ElementBy(id = "saveProvider")
    PageElement saveProvider;

    @Override
    public TimedCondition isAt() {
        return allowedDomains.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/openid-configuration?op=add";
    }

    public AddProviderPage setName(final String name) {
        this.name.clear();
        this.name.type(name);
        return this;
    }

    public AddProviderPage setEndpointUrl(final String s) {
        this.endpointUrl.clear();
        this.endpointUrl.type(s);
        return this;
    }

    public ConfigurationPage save() {
        saveProvider.click();
        return pageBinder.bind(ConfigurationPage.class);
    }

    public TimedQuery<String> getName() {
        return name.timed().getValue();
    }

    public TimedQuery<String> getEndpointUrl() {
        return endpointUrl.timed().getValue();
    }
}

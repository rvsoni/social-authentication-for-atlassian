package it.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class ConfigurationPage extends AbstractJiraAdminPage {

    @ElementBy(id = "openid")
    PageElement openIdWrapper;

    @ElementBy(id = "creatingUsersState")
    PageElement creatingUsersState;

    @ElementBy(id = "switchCreatingUsers")
    PageElement switchCreatingUsers;

    @ElementBy(id = "advanced")
    PageElement advanced;

    @ElementBy(id = "addProvider")
    PageElement addProvider;

    @Override
    public String linkId() {
        return "openid";
    }

    @Override
    public TimedCondition isAt() {
        return openIdWrapper.timed().isPresent();
    }

    @Override
    public String getUrl() {
        return "/plugins/servlet/openid-configuration";
    }


}

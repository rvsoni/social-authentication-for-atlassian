package it.confluence.pageobjects;

public class ConfluenceLoginPage extends it.jira.pageobjects.JiraLoginPage {
    @Override
    public String getUrl() {
        return "/login.action";
    }
}

package com.pawelniewiadomski.servicedesk.servlet;

import com.atlassian.webresource.api.assembler.WebResourceAssembler;

public class ConfigurationServlet extends com.pawelniewiadomski.jira.openid.authentication.servlet.ConfigurationServlet {

    @Override
    protected void configureAssembler(WebResourceAssembler assembler) {
        super.configureAssembler(assembler);
        assembler.data().requireData("openid.servicedesk", true);
    }

}

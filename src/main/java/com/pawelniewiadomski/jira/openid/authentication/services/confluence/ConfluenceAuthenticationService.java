package com.pawelniewiadomski.jira.openid.authentication.services.confluence;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Service
@ConfluenceComponent
@AllArgsConstructor
public class ConfluenceAuthenticationService implements AuthenticationService
{
    final GlobalSettings globalSettings;

    final TemplateHelper templateHelper;

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                            final OpenIdProvider provider, String identity, String email) throws IOException, ServletException
    {
        if (StringUtils.isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }
    }
}

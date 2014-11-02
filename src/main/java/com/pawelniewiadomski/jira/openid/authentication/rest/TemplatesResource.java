package com.pawelniewiadomski.jira.openid.authentication.rest;

import com.pawelniewiadomski.jira.openid.authentication.servlet.TemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;

@Path("templates")
@Produces({MediaType.TEXT_HTML})
public class TemplatesResource {

    @Autowired
    TemplateHelper templateHelper;

    @GET
    @Path("{templateName}")
    public Response getTemplate(@Context HttpServletRequest request, @PathParam("templateName") String templateName)
            throws ServletException, IOException {
        return Response.ok(templateHelper.render(request, templateName, Collections.<String, Object>emptyMap())).build();
    }
}

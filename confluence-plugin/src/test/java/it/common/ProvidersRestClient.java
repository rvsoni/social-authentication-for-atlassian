package it.common;

import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.ProductInstance;
import com.pawelniewiadomski.jira.openid.authentication.rest.responses.ProviderBean;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class ProvidersRestClient extends RestApiClient<ProvidersRestClient> {
    private final String rootPath;

    public ProvidersRestClient(@Nonnull ProductInstance productInstance) {
        super(new ProductInstanceBasedEnvironmentData(productInstance));
        this.rootPath = productInstance.getBaseUrl();
    }

    protected final WebResource createResourcePath() {
        WebResource resource = this.resourceRoot(this.rootPath)
                .path("rest").path("jira-openid-authentication").path("1.0").path("providers");
        return resource;
    }

    public List<ProviderBean> getProviders() throws JSONException {
        return this.createResourcePath().get(new GenericType<List<ProviderBean>>() {
        });
    }

    public void createProvider(ProviderBean provider) {
        this.createResourcePath().type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(provider);
    }
}

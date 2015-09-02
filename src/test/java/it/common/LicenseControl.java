package it.common;

import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.pageobjects.ProductInstance;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nonnull;

public class LicenseControl extends RestApiClient {
    private final String rootPath;

    public LicenseControl(@Nonnull ProductInstance productInstance) {
        super(new ProductInstanceBasedEnvironmentData(productInstance));
        this.rootPath = productInstance.getBaseUrl();
    }

    protected final WebResource createResourceForPath(String restModulePath, String restModuleVersion) {
        WebResource resource = this.resourceRoot(this.rootPath).path("rest").path(restModulePath).path(restModuleVersion);
        return resource;
    }

    protected final WebResource createResourceForPath(String restModulePath) {
        return this.createResourceForPath(restModulePath, "1.0");
    }

    public void setPluginLicense(String pluginKey, String license) throws JSONException {
        pluginKey = pluginKey + "-key";
        JSONObject licenseDetails = new JSONObject();
        licenseDetails.put("rawLicense", license);
        this.createResourceForPath("plugins").path(pluginKey + "/license")
                .accept(new String[]{"application/vnd.atl.plugins+json"}).type("application/vnd.atl.plugins+json")
                .put(licenseDetails.toString());
    }

}

package it.common;

import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.pageobjects.ProductInstance;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nonnull;

public class PluginsRestClient extends RestApiClient<PluginsRestClient> {
    private final String rootPath;

    public PluginsRestClient(@Nonnull ProductInstance productInstance) {
        super(new ProductInstanceBasedEnvironmentData(productInstance));
        this.rootPath = productInstance.getBaseUrl();
    }

    protected final WebResource createResourcePath() {
        WebResource resource = this.resourceRoot(this.rootPath).path("rest").path("plugins").path("1.0");
        return resource;
    }

    public boolean isPluginLicenseValid(String pluginKey) throws JSONException {
        pluginKey = pluginKey + "-key";
        JSONObject license = new JSONObject(this.createResourcePath().path(pluginKey).path("license").get(String.class));
        return (Boolean) license.get("valid") == true;
    }

    public void setPluginLicenseIfInvalid(String pluginKey, String license) throws JSONException {
        if (!isPluginLicenseValid(pluginKey)) {
            setPluginLicense(pluginKey, license);
        }
    }

    public void setPluginLicense(String pluginKey, String license) throws JSONException {
        pluginKey = pluginKey + "-key";
        JSONObject licenseDetails = new JSONObject();
        licenseDetails.put("rawLicense", license);
        this.createResourcePath().path(pluginKey).path("license")
                .accept(new String[]{"application/vnd.atl.plugins+json"}).type("application/vnd.atl.plugins+json")
                .put(licenseDetails.toString());
    }

}

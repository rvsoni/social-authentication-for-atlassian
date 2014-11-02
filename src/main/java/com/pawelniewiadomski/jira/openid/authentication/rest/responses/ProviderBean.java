package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderBean extends BasicProviderBean {

    @JsonProperty
    private String allowedDomains;

    @JsonProperty
    private Integer ordering;

    @JsonProperty
    private boolean enabled;

    @JsonProperty
    private String clientId;

    @JsonProperty
    private String clientSecret;

    @JsonProperty
    private String endpointUrl;

    @JsonProperty
    private String callbackId;

    @JsonProperty
    private String extensionNamespace;

    public ProviderBean() {
    }

    public ProviderBean(Integer id, String name, boolean enabled, Integer ordering, String allowedDomains, String providerType) {
        super(id, name, providerType);
        this.enabled = enabled;
        this.ordering = ordering == null ? 1 : ordering;
        this.allowedDomains = allowedDomains;
    }

    public ProviderBean(OpenIdProvider provider) {
        this(provider.getID(), provider.getName(), provider.isEnabled(), provider.getOrdering(),
                provider.getAllowedDomains(), provider.getProviderType());
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(final int ordering) {
        this.ordering = ordering;
    }

    public String getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(final String allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public String getExtensionNamespace() {
        return extensionNamespace;
    }

    public String getCallbackId() {
        return callbackId;
    }
}
package it.common;

import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@SuppressWarnings("unused")
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class ProviderBean {
    final int id;

    final String name;

    final String providerType;

    final String allowedDomains;

    final Integer ordering;

    final boolean enabled;

    final String clientId;

    final String clientSecret;

    final String endpointUrl;

    final String callbackId;

    final String extensionNamespace;

    @java.beans.ConstructorProperties({"id", "name", "providerType", "allowedDomains", "ordering", "enabled", "clientId", "clientSecret", "endpointUrl", "callbackId", "extensionNamespace"})
    public ProviderBean(@JsonProperty("id") int id,
                        @JsonProperty("name") String name,
                        @JsonProperty("providerType") String providerType,
                        @JsonProperty("allowedDomains") String allowedDomains,
                        @JsonProperty("ordering") Integer ordering,
                        @JsonProperty("enabled") boolean enabled,
                        @JsonProperty("clientId") String clientId,
                        @JsonProperty("clientSecret") String clientSecret,
                        @JsonProperty("endpointUrl") String endpointUrl,
                        @JsonProperty("callbackId") String callbackId,
                        @JsonProperty("extensionNamespace") String extensionNamespace) {
        this.id = id;
        this.name = name;
        this.providerType = providerType;
        this.allowedDomains = allowedDomains;
        this.ordering = ordering;
        this.enabled = enabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.endpointUrl = endpointUrl;
        this.callbackId = callbackId;
        this.extensionNamespace = extensionNamespace;
    }
}
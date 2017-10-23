package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.providers.DiscoverablyOauth2ProviderType;
import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.apache.commons.lang.StringUtils.defaultString;

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

    final String prompt;

    final String scope;

    @java.beans.ConstructorProperties({"id", "name", "providerType", "allowedDomains", "ordering", "enabled",
            "clientId", "clientSecret", "endpointUrl", "callbackId", "extensionNamespace"})
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
                        @JsonProperty("extensionNamespace") String extensionNamespace,
                        @JsonProperty("prompt") String prompt,
                        @JsonProperty("scope") String scope) {
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
        this.prompt = prompt;
        this.scope = scope;
    }


    public static ProviderBean of(OpenIdProvider provider) {
        return new ProviderBean(provider.getID(), provider.getName(), provider.getProviderType(),
                provider.getAllowedDomains(),
                provider.getOrdering() == null ? 1 : provider.getOrdering(),
                provider.isEnabled(), provider.getClientId(), provider.getClientSecret(),
                provider.getEndpointUrl(), provider.getCallbackId(), provider.getExtensionNamespace(),
                defaultString(provider.getPrompt(), DiscoverablyOauth2ProviderType.SELECT_ACCOUNT_PROMPT),
                defaultString(provider.getScope(), DiscoverablyOauth2ProviderType.DEFAULT_SCOPE));
    }
}
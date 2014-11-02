package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@SuppressWarnings("unused")
@JsonRootName("openIdProvider")
public class BasicProviderBean {

    @JsonProperty
    private int id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String providerType;

    public BasicProviderBean() {}

    public BasicProviderBean(int id, String name, String providerType) {
        this.id = id;
        this.name = name;
        this.providerType = providerType;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProviderType()
    {
        return providerType;
    }

    public void setProviderType(String providerType)
    {
        this.providerType = providerType;
    }
}
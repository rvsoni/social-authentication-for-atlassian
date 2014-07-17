package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "openIdProvider")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicProviderResponse {

    @XmlElement(name =  "id")
    private int id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "providerType")
    private String providerType;

    public BasicProviderResponse(int id, String name, String providerType) {
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
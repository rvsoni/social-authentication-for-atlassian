package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "openIdProvider")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicProviderResponse {

    @XmlElement(name =  "id")
    private int id;

    @XmlElement(name = "name")
    private String name;

    public BasicProviderResponse(int id, String name) {
        this.id = id;
        this.name = name;
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
}
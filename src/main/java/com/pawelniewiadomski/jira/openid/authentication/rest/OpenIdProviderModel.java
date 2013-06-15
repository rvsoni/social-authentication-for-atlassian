package com.pawelniewiadomski.jira.openid.authentication.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "openIdProvider")
@XmlAccessorType(XmlAccessType.FIELD)
public class OpenIdProviderModel {

    @XmlElement(name =  "id")
    private Integer id;

    @XmlElement(name = "name")
    private String name;

    public OpenIdProviderModel() {
    }

    public OpenIdProviderModel(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
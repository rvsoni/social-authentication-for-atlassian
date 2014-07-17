package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "openIdProvider")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderResponse extends BasicProviderResponse {

    @XmlElement
    private boolean enabled;

    @XmlElement
    private int ordering;

    @XmlElement
    private boolean internal;

    @XmlElement
    private String allowedDomains;

    public ProviderResponse(Integer id, String name, boolean enabled, Integer ordering, boolean internal, String allowedDomains, String providerType) {
        super(id, name, providerType);
        this.enabled = enabled;
        this.ordering = ordering == null ? 1 : ordering;
        this.internal = internal;
        this.allowedDomains = allowedDomains;
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

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(final boolean internal) {
        this.internal = internal;
    }

    public String getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(final String allowedDomains) {
        this.allowedDomains = allowedDomains;
    }


}
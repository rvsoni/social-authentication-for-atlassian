package com.pawelniewiadomski.jira.openid.authentication.providers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

@JsonRootName("errors")
public class Errors {
    @JsonProperty
    private Collection<String> errorMessages = Lists.newArrayList();
    @JsonProperty
    private Map<String, String> errors = Maps.newHashMap();

    public Errors addErrorMessage(@Nonnull String errorMessage) {
        this.errorMessages.add(errorMessage);
        return this;
    }

    public Errors addErrorMessages(@Nonnull Collection<String> messages) {
        this.errorMessages.addAll(messages);
        return this;
    }

    public Errors addError(@Nonnull String field, @Nonnull String message) {
        this.errors.put(field, message);
        return this;
    }

    public boolean hasAnyErrors() {
        return !this.errorMessages.isEmpty() || !this.errors.isEmpty();
    }

    public Collection<String> getErrorMessages() {
        return this.errorMessages;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

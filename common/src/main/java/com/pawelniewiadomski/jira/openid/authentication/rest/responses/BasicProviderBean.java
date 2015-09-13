package com.pawelniewiadomski.jira.openid.authentication.rest.responses;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("openIdProvider")
@JsonAutoDetect
@Data
public class BasicProviderBean {
    final int id;

    final String name;

    final String providerType;
}
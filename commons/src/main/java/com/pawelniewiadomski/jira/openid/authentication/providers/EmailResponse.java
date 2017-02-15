package com.pawelniewiadomski.jira.openid.authentication.providers;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailResponse {
    private String email;
    private boolean primary;
    private boolean verified;
}

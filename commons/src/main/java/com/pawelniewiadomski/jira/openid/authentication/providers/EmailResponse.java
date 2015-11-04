package com.pawelniewiadomski.jira.openid.authentication.providers;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@Data
@JsonAutoDetect
public class EmailResponse {
    private String email;
    private boolean primary;
    private boolean verified;
}

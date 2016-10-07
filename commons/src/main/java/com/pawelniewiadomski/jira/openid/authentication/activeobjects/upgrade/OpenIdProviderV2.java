package com.pawelniewiadomski.jira.openid.authentication.activeobjects.upgrade;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.*;

@SuppressWarnings("UnusedDeclaration")
@Table("openid_providers")
@Preload
public interface OpenIdProviderV2 extends Entity {

    int MAX_LENGTH = 450;

    String GOOGLE_TYPE = "google";
    String OPENID_TYPE = "openid1";
    String OAUTH2_TYPE = "oauth2";
    String FACEBOOK_TYPE = "facebook";
    String LINKED_IN_TYPE = "linkedin";
    String GITHUB_TYPE = "github";
    String VK_TYPE = "vk";

    String ID = "ID";
    String NAME = "NAME";
    String ENDPOINT_URL = "ENDPOINT_URL";
    String EXTENSION_NAMESPACE = "EXTENSION_NAMESPACE";
    String ENABLED = "ENABLED";
    String CREATE_USERS = "CREATE_USERS";
    String ALLOWED_DOMAINS = "ALLOWED_DOMAINS";
    String ORDERING = "ORDERING";
    String PROVIDER_TYPE = "PROVIDER_TYPE";
    String CALLBACK_ID = "CALLBACK_ID";
    String CLIENT_ID = "CLIENT_ID";
    String CLIENT_SECRET = "CLIENT_SECRET";
    String PROMPT = "PROMPT";

    @NotNull
    @Unique
    @StringLength(value = 100)
    String getName();
    void setName(final String name);

    @NotNull
    @StringLength(value = MAX_LENGTH)
    String getEndpointUrl();
    void setEndpointUrl(String endpointUrl);

    @StringLength(value = 25)
    String getExtensionNamespace();
    void setExtensionNamespace(String namespace);

    @NotNull
    @Indexed
    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isConnect();
    void setConnect(boolean connect);

    @NotNull
    @Default("TRUE")
    boolean isCreateUsers();
    void setCreateUsers(boolean createUsers);

    @StringLength(MAX_LENGTH)
    String getAllowedDomains();
    void setAllowedDomains(String allowedDomains);

    Integer getOrdering();
    void setOrdering(Integer ordering);

    @StringLength(MAX_LENGTH)
    String getProviderType();
    void setProviderType(String type);

    @StringLength(MAX_LENGTH)
    String getClientId();
    void setClientId(String clientId);

    @StringLength(MAX_LENGTH)
    String getClientSecret();
    void setClientSecret(String clientSecret);

    @StringLength(value = 68)
    String getCallbackId();
    void setCallbackId(String callbackId);

    String getPrompt();
    void setPrompt(String prompt);
}

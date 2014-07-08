package com.pawelniewiadomski.jira.openid.authentication.activeobjects;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.*;

@SuppressWarnings("UnusedDeclaration")
@Table("openid_providers")
@Preload
public interface OpenIdProvider extends Entity {

    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String ENDPOINT_URL = "ENDPOINT_URL";
    public static final String EXTENSION_NAMESPACE = "EXTENSION_NAMESPACE";
    public static final String ENABLED = "ENABLED";
    public static final String INTERNAL = "INTERNAL";
    public static final String CREATE_USERS = "CREATE_USERS";
    public static final String ALLOWED_DOMAINS = "ALLOWED_DOMAINS";
    public static final String ORDERING = "ORDERING";
    public static final String CONNECT = "CONNECT";

    @NotNull
    @Unique
    @StringLength(value = 100)
    String getName();
    void setName(final String name);

    @NotNull
    @StringLength(value = StringLength.MAX_LENGTH)
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
    @Default("FALSE")
    boolean isInternal();
    void setInternal(boolean internal);

    @NotNull
    @Default("TRUE")
    boolean isCreateUsers();
    void setCreateUsers(boolean createUsers);

    @StringLength(StringLength.MAX_LENGTH)
    String getAllowedDomains();
    void setAllowedDomains(String allowedDomains);

    Integer getOrdering();
    void setOrdering(Integer ordering);
}

package com.pawelniewiadomski.jira.openid.authentication;

import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdDao;
import com.pawelniewiadomski.jira.openid.activeobjects.OpenIdProvider;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class CustomOpenIdManager extends OpenIdManager {

	final OpenIdDao openIdDao;

	public CustomOpenIdManager(OpenIdDao openIdDao) {
		this.openIdDao = openIdDao;
	}

	protected Map<String, Endpoint> getEndpointCache() {
		Field cacheField = null;
		try {
			cacheField = OpenIdManager.class.getDeclaredField("endpointCache");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		cacheField.setAccessible(true);
		try {
			return (Map<String, Endpoint>) cacheField.get(this);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Lookup end point by name or full URL.
	 */
	public Endpoint lookupEndpoint(Integer providerId) {
		OpenIdProvider provider = null;
		try {
			provider = openIdDao.findProvider(providerId);
		} catch (SQLException e) {
			// fail bellow
		}
		if (provider == null) {
			throw new OpenIdException("Cannot find OP URL by Provider ID: " + providerId);
		}

		Endpoint endpoint = getEndpointCache().get(provider.getEndpointUrl());
		if (endpoint != null && !endpoint.isExpired())
			return endpoint;
		endpoint = requestEndpoint(url, alias==null ? Endpoint.DEFAULT_ALIAS : alias);
		endpointCache.put(url, endpoint);
		return endpoint;
	}
}

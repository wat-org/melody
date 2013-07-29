package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.FileAttribute;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.TextContent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ResourceAttribute<T> implements FileAttribute<T> {

	private static final String SCOPE_ATTR = "scope";

	private Scopes _scopes = Scopes.ALL;
	private String _value = null;

	public ResourceAttribute() {
	}

	@Override
	public String toString() {
		return name() + "=" + getStringValue();
	}

	public Scopes getScopes() {
		return _scopes;
	}

	@Attribute(name = SCOPE_ATTR)
	public Scopes setScopes(Scopes scopes) {
		if (scopes == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Scopes.class.getCanonicalName()
					+ ".");
		}
		Scopes previous = getScopes();
		_scopes = scopes;
		return previous;
	}

	public String getStringValue() {
		return _value;
	}

	@TextContent(mandatory = true)
	public String setStringValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getStringValue();
		_value = value;
		return previous;
	}

}
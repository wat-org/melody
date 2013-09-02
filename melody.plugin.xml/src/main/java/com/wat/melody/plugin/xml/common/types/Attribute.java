package com.wat.melody.plugin.xml.common.types;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Attribute {

	public static final String NAME_ATTR = "name";

	private String _attributeName = null;

	@Override
	public String toString() {
		return _attributeName;
	}

	public String getAttributeName() {
		return _attributeName;
	}

	@com.wat.melody.api.annotation.Attribute(name = NAME_ATTR, mandatory = true)
	public String setAttributeName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getAttributeName();
		_attributeName = name;
		return previous;
	}

}
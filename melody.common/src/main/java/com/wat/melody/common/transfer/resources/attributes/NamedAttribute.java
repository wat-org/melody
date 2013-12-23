package com.wat.melody.common.transfer.resources.attributes;

import com.wat.melody.api.annotation.Attribute;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NamedAttribute extends AttributeBase<String> {

	private static final String NAME_ATTR = "name";

	private String _name = null;

	public NamedAttribute() {
		super();
	}

	public String getName() {
		return _name;
	}

	@Attribute(name = NAME_ATTR, mandatory = true)
	public String setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an attribute name).");
		}
		String previous = getName();
		_name = name;
		return previous;
	}

	@Override
	public String name() {
		return getName();
	}

	@Override
	public String value() {
		return getStringValue();
	}

}
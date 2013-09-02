package com.wat.melody.plugin.xml.common.types;

import com.wat.melody.api.annotation.TextContent;
import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Condition {

	private String _condition = null;

	public abstract boolean matches(Doc doc);

	public String getCondition() {
		return _condition;
	}

	@TextContent(mandatory = true)
	public String setCondition(String condition) {
		if (condition == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getCondition();
		_condition = condition.trim();
		return previous;
	}

}
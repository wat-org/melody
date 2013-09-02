package com.wat.melody.plugin.xml.common.types;

import com.wat.melody.api.annotation.TextContent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ElementsSelector {

	private String _elementSelector = null;

	public String getElementsSelector() {
		return _elementSelector;
	}

	@TextContent(mandatory = true)
	public String setElementsSelector(String elementsSelector) {
		if (elementsSelector == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getElementsSelector();
		_elementSelector = elementsSelector.trim();
		return previous;
	}

}
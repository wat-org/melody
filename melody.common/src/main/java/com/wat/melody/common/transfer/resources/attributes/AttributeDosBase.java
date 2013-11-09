package com.wat.melody.common.transfer.resources.attributes;

import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AttributeDosBase extends AttributeBase<Boolean> {

	private boolean _boolValue = false;

	public AttributeDosBase() {
	}

	@Override
	public String setStringValue(final String value) throws MelodyException {
		String previous = super.setStringValue(value);
		// validate and convert input string to typed data
		_boolValue = Bool.parseString(value);
		return previous;
	}

	@Override
	public Boolean value() {
		return _boolValue;
	}

}
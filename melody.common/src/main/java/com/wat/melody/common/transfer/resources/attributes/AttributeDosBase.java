package com.wat.melody.common.transfer.resources.attributes;

import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AttributeDosBase extends AttributeBase<Boolean> {

	/*
	 * Boolean value (_boolValue) and string value (super._value) must contains
	 * the same data.
	 */
	private boolean _boolValue = false;

	public AttributeDosBase() {
		super();
		/*
		 * Because the boolean value is initialized to false, the string value
		 * must be set to the corresponding string value.
		 */
		try {
			super.setStringValue(value().toString());
		} catch (MelodyException cantberaised) {
			throw new RuntimeException("This method shouldn't raise any "
					+ "exception. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
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
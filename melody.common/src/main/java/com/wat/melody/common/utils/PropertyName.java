package com.wat.melody.common.utils;

import com.wat.melody.common.utils.exception.IllegalPropertyNameException;

public class PropertyName {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link PropertyName} object.
	 * </p>
	 * 
	 * @param sPropertyName
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>PropertyName</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalPropertyNameException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>PropertyName</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static PropertyName parseString(String sPropertyName)
			throws IllegalPropertyNameException {
		return new PropertyName(sPropertyName);
	}

	/**
	 * The pattern which the 'name' must statisfied
	 */
	public static final String PATTERN = "\\w+([.]\\w+)*";

	private String msValue;

	public PropertyName(String sPropertyName)
			throws IllegalPropertyNameException {
		setValue(sPropertyName);
	}

	@Override
	public String toString() {
		return msValue;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropertyName) {
			PropertyName pn = (PropertyName) anObject;
			return getValue().equals(pn.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	private String setValue(String sPropertyName)
			throws IllegalPropertyNameException {
		if (sPropertyName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a PropertyName).");
		}
		if (sPropertyName.trim().length() == 0) {
			throw new IllegalPropertyNameException(Messages.bind(
					Messages.PropertyNameEx_EMPTY, sPropertyName));
		} else if (!sPropertyName.matches("^" + PATTERN + "$")) {
			throw new IllegalPropertyNameException(Messages.bind(
					Messages.PropertyNameEx_INVALID, sPropertyName, PATTERN));
		}
		String previous = getValue();
		msValue = sPropertyName;
		return previous;
	}
}

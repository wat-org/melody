package com.wat.melody.common.properties;

import com.wat.melody.common.properties.exception.IllegalPropertyNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PropertyName {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link PropertyName} object.
	 * </p>
	 * 
	 * @param propertyName
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link PropertyName} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalPropertyNameException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link PropertyName}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static PropertyName parseString(String propertyName)
			throws IllegalPropertyNameException {
		return new PropertyName(propertyName);
	}

	/**
	 * The pattern which the 'name' must satisfied
	 */
	public static final String PATTERN = "\\w+([.]\\w+)*";

	private String msValue;

	public PropertyName(String propertyName)
			throws IllegalPropertyNameException {
		setValue(propertyName);
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

	private String setValue(String propertyName)
			throws IllegalPropertyNameException {
		if (propertyName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a PropertyName).");
		}
		if (propertyName.trim().length() == 0) {
			throw new IllegalPropertyNameException(Messages.bind(
					Messages.PropertyNameEx_EMPTY, propertyName));
		} else if (!propertyName.matches("^" + PATTERN + "$")) {
			throw new IllegalPropertyNameException(Messages.bind(
					Messages.PropertyNameEx_INVALID, propertyName, PATTERN));
		}
		String previous = getValue();
		msValue = propertyName;
		return previous;
	}
}

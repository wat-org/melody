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
	 * Convert the given <tt>String</tt> to an {@link PropertyName} object.
	 * </p>
	 * 
	 * @param propertyName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PropertyName} object, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalPropertyNameException
	 *             if the given input <tt>String</tt> is not a valid
	 *             {@link PropertyName}.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 */
	public static PropertyName parseString(String propertyName)
			throws IllegalPropertyNameException {
		return new PropertyName(propertyName);
	}

	/**
	 * The pattern which the 'name' must satisfied
	 */
	public static final String PATTERN = "\\w+([.]\\w+)*";

	private String _value;

	public PropertyName(String propertyName)
			throws IllegalPropertyNameException {
		setValue(propertyName);
	}

	@Override
	public String toString() {
		return _value;
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
		return _value;
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
		_value = propertyName;
		return previous;
	}
}

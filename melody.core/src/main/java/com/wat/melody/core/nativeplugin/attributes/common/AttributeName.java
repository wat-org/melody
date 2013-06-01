package com.wat.melody.core.nativeplugin.attributes.common;

import com.wat.melody.common.xml.DUNIDDoc;
import com.wat.melody.core.nativeplugin.attributes.common.exception.IllegalAttributeNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributeName {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link AttributeName} object.
	 * </p>
	 * 
	 * @param attributeName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link AttributeName} object, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalAttributeNameException
	 *             if the given input <tt>String</tt> is not a valid
	 *             {@link AttributeName}.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 */
	public static AttributeName parseString(String attributeName)
			throws IllegalAttributeNameException {
		return new AttributeName(attributeName);
	}

	/**
	 * The pattern an AttributeName must satisfy.
	 */
	public static final String PATTERN = "\\w+([.-_]\\w+)*";

	private String _value;

	public AttributeName(String attributeName)
			throws IllegalAttributeNameException {
		setValue(attributeName);
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
		if (anObject instanceof AttributeName) {
			AttributeName on = (AttributeName) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	public String setValue(String attributeName)
			throws IllegalAttributeNameException {
		if (attributeName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ AttributeName.class.getCanonicalName() + ").");
		}
		if (attributeName.trim().length() == 0) {
			throw new IllegalAttributeNameException(Messages.bind(
					Messages.AttributeNameEx_EMPTY, attributeName));
		} else if (!attributeName.matches("^" + PATTERN + "$")) {
			throw new IllegalAttributeNameException(Messages.bind(
					Messages.AttributeNameEx_INVALID, attributeName, PATTERN));
		} else if (attributeName.equals(DUNIDDoc.DUNID_ATTR)) {
			throw new IllegalAttributeNameException(Messages.bind(
					Messages.AttributeNameEx_DUNID_FORBIDDEN, attributeName,
					DUNIDDoc.DUNID_ATTR));
		}
		String previous = toString();
		_value = attributeName;
		return previous;
	}

}
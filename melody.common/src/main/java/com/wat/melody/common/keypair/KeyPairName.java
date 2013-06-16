package com.wat.melody.common.keypair;

import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairName {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link KeyPairName} object.
	 * </p>
	 * 
	 * @param sKeyPairName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link KeyPairName} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalKeyPairNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't mathc the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static KeyPairName parseString(String sKeyPairName)
			throws IllegalKeyPairNameException {
		return new KeyPairName(sKeyPairName);
	}

	/**
	 * The pattern a KeyPairName must satisfy.
	 */
	public static final String PATTERN = "[.\\d\\w-_/\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	private String _value;

	public KeyPairName(String sKeyPairName) throws IllegalKeyPairNameException {
		setValue(sKeyPairName);
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
		if (anObject instanceof KeyPairName) {
			KeyPairName on = (KeyPairName) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String sKeyPairName)
			throws IllegalKeyPairNameException {
		if (sKeyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ KeyPairName.class.getCanonicalName() + ").");
		}
		if (sKeyPairName.trim().length() == 0) {
			throw new IllegalKeyPairNameException(Msg.bind(
					Messages.KeyPairNameEx_EMPTY, sKeyPairName));
		} else if (!sKeyPairName.matches("^" + PATTERN + "$")) {
			throw new IllegalKeyPairNameException(Msg.bind(
					Messages.KeyPairNameEx_INVALID, sKeyPairName, PATTERN));
		}
		String previous = toString();
		_value = sKeyPairName;
		return previous;
	}

}
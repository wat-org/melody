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
	 * @param keyPairName
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
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static KeyPairName parseString(String keyPairName)
			throws IllegalKeyPairNameException {
		return new KeyPairName(keyPairName);
	}

	/**
	 * The pattern a KeyPairName must satisfy.
	 */
	public static final String PATTERN = "[.\\d\\w-_/\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	private String _value;

	public KeyPairName(String keyPairName) throws IllegalKeyPairNameException {
		setValue(keyPairName);
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

	private String setValue(String keyPairName)
			throws IllegalKeyPairNameException {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ KeyPairName.class.getCanonicalName() + ").");
		}
		if (keyPairName.trim().length() == 0) {
			throw new IllegalKeyPairNameException(Msg.bind(
					Messages.KeyPairNameEx_EMPTY, keyPairName));
		} else if (!keyPairName.matches("^" + PATTERN + "$")) {
			throw new IllegalKeyPairNameException(Msg.bind(
					Messages.KeyPairNameEx_INVALID, keyPairName, PATTERN));
		}
		String previous = toString();
		_value = keyPairName;
		return previous;
	}

}
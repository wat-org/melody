package com.wat.melody.common.keypair;

import com.wat.melody.common.keypair.exception.IllegalKeyPairSizeException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairSize {

	/**
	 * <p>
	 * Convert the given size to a {@link KeyPairSize} object.
	 * </p>
	 * 
	 * @param keyPairSize
	 *            is the size to convert.
	 * 
	 * @return a {@link KeyPairSize}, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>int</tt> is < 1024.
	 */
	public static KeyPairSize parseInt(int keyPairSize)
			throws IllegalKeyPairSizeException {
		return new KeyPairSize(keyPairSize);
	}

	/**
	 * <p>
	 * Convert the given size to a {@link KeyPairSize} object.
	 * </p>
	 * 
	 * @param keyPairSize
	 *            is the size to convert.
	 * 
	 * @return a {@link KeyPairSize}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is < 1024 ;</li>
	 *             </ul>
	 */
	public static KeyPairSize parseString(String keyPairSize)
			throws IllegalKeyPairSizeException {
		return new KeyPairSize(keyPairSize);
	}

	private int _value;

	public KeyPairSize(int keyPairSize) throws IllegalKeyPairSizeException {
		setKeyPairSize(keyPairSize);
	}

	public KeyPairSize(String keyPairSize) throws IllegalKeyPairSizeException {
		setKeyPairSize(keyPairSize);
	}

	@Override
	public int hashCode() {
		return getValue();
	}

	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof KeyPairSize) {
			KeyPairSize keyPairSize = (KeyPairSize) anObject;
			return getValue() == keyPairSize.getValue();
		}
		return false;
	}

	/**
	 * @return the KeyPairSize's value.
	 */
	public int getValue() {
		return _value;
	}

	private int setKeyPairSize(int keyPairSize)
			throws IllegalKeyPairSizeException {
		if (keyPairSize < 1024) {
			throw new IllegalKeyPairSizeException(Msg.bind(
					Messages.KeyPairSizeEx_TOO_SMALL, keyPairSize));
		}
		int previous = getValue();
		_value = keyPairSize;
		return previous;
	}

	private int setKeyPairSize(String keyPairSize)
			throws IllegalKeyPairSizeException {
		if (keyPairSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ KeyPairSize.class.getCanonicalName() + ").");
		}
		if (keyPairSize.trim().length() == 0) {
			throw new IllegalKeyPairSizeException(Msg.bind(
					Messages.KeyPairSizeEx_EMPTY, keyPairSize));
		}
		try {
			return setKeyPairSize(Integer.parseInt(keyPairSize));
		} catch (NumberFormatException Ex) {
			throw new IllegalKeyPairSizeException(Msg.bind(
					Messages.KeyPairSizeEx_NOT_A_NUMBER, keyPairSize));
		}
	}

}
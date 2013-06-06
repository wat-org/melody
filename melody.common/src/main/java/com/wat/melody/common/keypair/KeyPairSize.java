package com.wat.melody.common.keypair;

import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.keypair.exception.IllegalKeyPairSizeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairSize {

	/**
	 * @param iKeyPairSize
	 * 
	 * @return a {@link KeyPairSize}, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>int</tt> is < 1024.
	 */
	public static KeyPairSize parseInt(int iKeyPairSize)
			throws IllegalKeyPairSizeException {
		return new KeyPairSize(iKeyPairSize);
	}

	/**
	 * @param sKeyPairSize
	 * 
	 * @return a {@link KeyPairSize}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>String</tt> is < 1024.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static KeyPairSize parseString(String sKeyPairSize)
			throws IllegalKeyPairSizeException {
		return new KeyPairSize(sKeyPairSize);
	}

	private int _value;

	/**
	 * @param iKeyPairSize
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>int</tt> is < 1024.
	 */
	public KeyPairSize(int iKeyPairSize) throws IllegalKeyPairSizeException {
		setKeyPairSize(iKeyPairSize);
	}

	/**
	 * @param sKeyPairSize
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>String</tt> is < 1024.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	public KeyPairSize(String sKeyPairSize) throws IllegalKeyPairSizeException {
		setKeyPairSize(sKeyPairSize);
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

	/**
	 * @param iKeyPairSize
	 * 
	 * @return the previous KeyPairSize's value.
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>int</tt> is < 1024.
	 */
	private int setKeyPairSize(int iKeyPairSize)
			throws IllegalKeyPairSizeException {
		if (iKeyPairSize < 1024) {
			throw new IllegalKeyPairSizeException(Messages.bind(
					Messages.KeyPairSizeEx_TOO_SMALL, iKeyPairSize));
		}
		int previous = getValue();
		_value = iKeyPairSize;
		return previous;
	}

	/**
	 * @param sKeyPairSize
	 * 
	 * @return the previous KeyPairSize's value.
	 * 
	 * @throws IllegalKeyPairSizeException
	 *             if the given <tt>String</tt> is < 1024.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	private int setKeyPairSize(String sKeyPairSize)
			throws IllegalKeyPairSizeException {
		if (sKeyPairSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ KeyPairSize.class.getCanonicalName() + ").");
		}
		if (sKeyPairSize.trim().length() == 0) {
			throw new IllegalKeyPairSizeException(Messages.bind(
					Messages.KeyPairSizeEx_EMPTY, sKeyPairSize));
		}
		try {
			return setKeyPairSize(Integer.parseInt(sKeyPairSize));
		} catch (NumberFormatException Ex) {
			throw new IllegalKeyPairSizeException(Messages.bind(
					Messages.KeyPairSizeEx_NOT_A_NUMBER, sKeyPairSize));
		}
	}

}
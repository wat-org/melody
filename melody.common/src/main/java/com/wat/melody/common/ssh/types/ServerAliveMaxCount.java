package com.wat.melody.common.ssh.types;

import com.wat.melody.common.ssh.types.exception.IllegalServerAliveMaxCountException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ServerAliveMaxCount {

	/**
	 * 
	 * @param sTimeout
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input int is < 0.
	 */
	public static ServerAliveMaxCount parseInt(int iValue)
			throws IllegalServerAliveMaxCountException {
		return new ServerAliveMaxCount(iValue);
	}

	/**
	 * @param sValue
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static ServerAliveMaxCount parseString(String sValue)
			throws IllegalServerAliveMaxCountException {
		return new ServerAliveMaxCount(sValue);
	}

	private int _value;

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input int is < 0.
	 */
	public ServerAliveMaxCount(int iValue)
			throws IllegalServerAliveMaxCountException {
		setValue(iValue);
	}

	/**
	 * @param sValue
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public ServerAliveMaxCount(String sValue)
			throws IllegalServerAliveMaxCountException {
		setTimeout(sValue);
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
		if (anObject instanceof ServerAliveMaxCount) {
			ServerAliveMaxCount maxcount = (ServerAliveMaxCount) anObject;
			return getValue() == maxcount.getValue();
		}
		return false;
	}

	/**
	 * 
	 * @return the value.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * 
	 * @param sTimeout
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input int is < 0.
	 */
	private int setValue(int iValue) throws IllegalServerAliveMaxCountException {
		if (iValue < 0) {
			throw new IllegalServerAliveMaxCountException(Messages.bind(
					Messages.ServerAliveMaxCountEx_NEGATIVE, iValue));
		}
		int previous = getValue();
		_value = iValue;
		return previous;
	}

	/**
	 * @param sValue
	 * 
	 * @return
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	private int setTimeout(String sValue)
			throws IllegalServerAliveMaxCountException {
		if (sValue == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ServerAliveMaxCount.class.getCanonicalName() + ").");
		}
		if (sValue.trim().length() == 0) {
			throw new IllegalServerAliveMaxCountException(Messages.bind(
					Messages.ServerAliveMaxCountEx_EMPTY, sValue));
		}
		try {
			return setValue(Integer.parseInt(sValue));
		} catch (NumberFormatException Ex) {
			throw new IllegalServerAliveMaxCountException(Messages.bind(
					Messages.ServerAliveMaxCountEx_NOT_A_NUMBER, sValue));
		}
	}

}

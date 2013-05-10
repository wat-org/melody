package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpCode {

	private static final String _ALL = "all";

	public static final IcmpCode ALL = createIcmpCode(_ALL);

	private static IcmpCode createIcmpCode(String sIcmpCode) {
		try {
			return new IcmpCode(sIcmpCode);
		} catch (IllegalIcmpCodeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "an Icmp Code with value '" + sIcmpCode + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * 
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input int is < 0.
	 */
	public static IcmpCode parseInt(int iIcmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(iIcmpCode);
	}

	/**
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static IcmpCode parseString(String sIcmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(sIcmpCode);
	}

	private int _value;

	/**
	 * 
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input int is < 0.
	 */
	public IcmpCode(int iIcmpCode) throws IllegalIcmpCodeException {
		setIcmpCode(iIcmpCode);
	}

	/**
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public IcmpCode(String sIcmpCode) throws IllegalIcmpCodeException {
		setIcmpCode(sIcmpCode);
	}

	@Override
	public int hashCode() {
		return getValue();
	}

	@Override
	public String toString() {
		return _value == -1 ? _ALL : String.valueOf(_value);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof IcmpCode) {
			IcmpCode IcmpCode = (IcmpCode) anObject;
			return getValue() == IcmpCode.getValue();
		}
		return false;
	}

	/**
	 * 
	 * @return the IcmpCode's value.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * 
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input int is < 0.
	 */
	private int setIcmpCode(int iIcmpCode) throws IllegalIcmpCodeException {
		if (iIcmpCode < -1) {
			throw new IllegalIcmpCodeException(Messages.bind(
					Messages.IcmpCodeEx_NEGATIVE, iIcmpCode));
		}
		int previous = getValue();
		_value = iIcmpCode;
		return previous;
	}

	/**
	 * @param sIcmpCode
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	private int setIcmpCode(String sIcmpCode) throws IllegalIcmpCodeException {
		if (sIcmpCode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ IcmpCode.class.getCanonicalName() + ").");
		}
		if (sIcmpCode.trim().length() == 0) {
			throw new IllegalIcmpCodeException(Messages.bind(
					Messages.IcmpCodeEx_EMPTY, sIcmpCode));
		}
		if (sIcmpCode.equals(_ALL)) {
			return setIcmpCode(-1);
		}
		try {
			return setIcmpCode(Integer.parseInt(sIcmpCode));
		} catch (NumberFormatException Ex) {
			throw new IllegalIcmpCodeException(Messages.bind(
					Messages.IcmpCodeEx_NOT_A_NUMBER, sIcmpCode));
		}
	}

}
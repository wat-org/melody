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
	 * @param iIcmpCode
	 * 
	 * @return an {@link IcmpCode} object, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public static IcmpCode parseInt(int iIcmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(iIcmpCode);
	}

	/**
	 * @param sIcmpCode
	 * 
	 * @return an {@link IcmpCode} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static IcmpCode parseString(String sIcmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(sIcmpCode);
	}

	private int _value;

	/**
	 * @param iIcmpCode
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public IcmpCode(int iIcmpCode) throws IllegalIcmpCodeException {
		setIcmpCode(iIcmpCode);
	}

	/**
	 * @param sIcmpCode
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
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
			IcmpCode icmpCode = (IcmpCode) anObject;
			return getValue() == icmpCode.getValue();
		}
		return false;
	}

	/**
	 * @return the IcmpCode's value.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * @param iIcmpCode
	 * 
	 * @return the previous IcmpCode's value.
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>int</tt> is < -1.
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
	 * @return the previous IcmpCode's value.
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
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
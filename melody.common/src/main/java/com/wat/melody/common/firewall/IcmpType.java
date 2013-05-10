package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpType {

	private static final String _ALL = "all";

	public static final IcmpType ALL = createIcmpType(_ALL);

	private static IcmpType createIcmpType(String sIcmpType) {
		try {
			return new IcmpType(sIcmpType);
		} catch (IllegalIcmpTypeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "an Icmp Type with value '" + sIcmpType + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * 
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input int is < 0.
	 */
	public static IcmpType parseInt(int iIcmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(iIcmpType);
	}

	/**
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static IcmpType parseString(String sIcmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(sIcmpType);
	}

	private int _value;

	/**
	 * 
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input int is < 0.
	 */
	public IcmpType(int iIcmpType) throws IllegalIcmpTypeException {
		setIcmpType(iIcmpType);
	}

	/**
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public IcmpType(String sIcmpType) throws IllegalIcmpTypeException {
		setIcmpType(sIcmpType);
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
		if (anObject instanceof IcmpType) {
			IcmpType IcmpType = (IcmpType) anObject;
			return getValue() == IcmpType.getValue();
		}
		return false;
	}

	/**
	 * 
	 * @return the IcmpType's value.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * 
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input int is < 0.
	 */
	private int setIcmpType(int iIcmpType) throws IllegalIcmpTypeException {
		if (iIcmpType < -1) {
			throw new IllegalIcmpTypeException(Messages.bind(
					Messages.IcmpTypeEx_NEGATIVE, iIcmpType));
		}
		int previous = getValue();
		_value = iIcmpType;
		return previous;
	}

	/**
	 * @param sIcmpType
	 * 
	 * @return
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	private int setIcmpType(String sIcmpType) throws IllegalIcmpTypeException {
		if (sIcmpType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ IcmpType.class.getCanonicalName() + ").");
		}
		if (sIcmpType.trim().length() == 0) {
			throw new IllegalIcmpTypeException(Messages.bind(
					Messages.IcmpTypeEx_EMPTY, sIcmpType));
		}
		if (sIcmpType.equals(_ALL)) {
			return setIcmpType(-1);
		}
		try {
			return setIcmpType(Integer.parseInt(sIcmpType));
		} catch (NumberFormatException Ex) {
			throw new IllegalIcmpTypeException(Messages.bind(
					Messages.IcmpTypeEx_NOT_A_NUMBER, sIcmpType));
		}
	}

}
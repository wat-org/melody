package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.messages.Msg;

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
	 * @param iIcmpType
	 * 
	 * @return an {@link IcmpType}, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public static IcmpType parseInt(int iIcmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(iIcmpType);
	}

	/**
	 * @param sIcmpType
	 * 
	 * @return an {@link IcmpType}, which is equal to the given <tt>String</tt>.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static IcmpType parseString(String sIcmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(sIcmpType);
	}

	private int _value;

	/**
	 * @param iIcmpType
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public IcmpType(int iIcmpType) throws IllegalIcmpTypeException {
		setIcmpType(iIcmpType);
	}

	/**
	 * @param sIcmpType
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
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
			IcmpType icmpType = (IcmpType) anObject;
			return getValue() == icmpType.getValue();
		}
		return false;
	}

	/**
	 * @return the IcmpType's value.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * @param iIcmpType
	 * 
	 * @return the previous IcmpType's value.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	private int setIcmpType(int iIcmpType) throws IllegalIcmpTypeException {
		if (iIcmpType < -1) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_NEGATIVE, iIcmpType));
		}
		int previous = getValue();
		_value = iIcmpType;
		return previous;
	}

	/**
	 * @param sIcmpType
	 * 
	 * @return the previous IcmpType's value.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>String</tt> is < -1.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 */
	private int setIcmpType(String sIcmpType) throws IllegalIcmpTypeException {
		if (sIcmpType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ IcmpType.class.getCanonicalName() + ").");
		}
		if (sIcmpType.trim().length() == 0) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_EMPTY, sIcmpType));
		}
		if (sIcmpType.equals(_ALL)) {
			return setIcmpType(-1);
		}
		try {
			return setIcmpType(Integer.parseInt(sIcmpType));
		} catch (NumberFormatException Ex) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_NOT_A_NUMBER, sIcmpType));
		}
	}

}
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
	 * <p>
	 * Convert the given <tt>int</tt> to an {@link IcmpType} object.
	 * </p>
	 * 
	 * @param icmpType
	 *            is the given <tt>int</tt> to convert. 0 is a magic value,
	 *            which means 'all codes'.
	 * 
	 * @return an {@link IcmpType} object, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalIcmpTypeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public static IcmpType parseInt(int icmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(icmpType);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link IcmpType} object.
	 * </p>
	 * 
	 * @param icmpType
	 *            is the given <tt>String</tt> to convert. "0" is a magic value,
	 *            which means 'all codes'.
	 * 
	 * @return an {@link IcmpType} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIcmpTypeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is < -1 ;</li>
	 *             </ul>
	 */
	public static IcmpType parseString(String icmpType)
			throws IllegalIcmpTypeException {
		return new IcmpType(icmpType);
	}

	private int _value;

	public IcmpType(int icmpType) throws IllegalIcmpTypeException {
		setIcmpType(icmpType);
	}

	public IcmpType(String icmpType) throws IllegalIcmpTypeException {
		setIcmpType(icmpType);
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
	 * @return this object's value.
	 */
	public int getValue() {
		return _value;
	}

	private int setIcmpType(int icmpType) throws IllegalIcmpTypeException {
		if (icmpType < -1) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_NEGATIVE, icmpType));
		}
		int previous = getValue();
		_value = icmpType;
		return previous;
	}

	private int setIcmpType(String icmpType) throws IllegalIcmpTypeException {
		if (icmpType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IcmpType.class.getCanonicalName() + ").");
		}
		if (icmpType.trim().length() == 0) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_EMPTY, icmpType));
		}
		if (icmpType.equals(_ALL)) {
			return setIcmpType(-1);
		}
		try {
			return setIcmpType(Integer.parseInt(icmpType));
		} catch (NumberFormatException Ex) {
			throw new IllegalIcmpTypeException(Msg.bind(
					Messages.IcmpTypeEx_NOT_A_NUMBER, icmpType));
		}
	}

}
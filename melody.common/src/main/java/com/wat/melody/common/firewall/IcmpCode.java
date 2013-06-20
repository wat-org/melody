package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.messages.Msg;

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
	 * <p>
	 * Convert the given <tt>int</tt> to an {@link IcmpCode} object.
	 * </p>
	 * 
	 * @param icmpCode
	 *            is the given <tt>int</tt> to convert. 0 is a magic value,
	 *            which means 'all codes'.
	 * 
	 * @return an {@link IcmpCode} object, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalIcmpCodeException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public static IcmpCode parseInt(int icmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(icmpCode);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link IcmpCode} object.
	 * </p>
	 * 
	 * @param icmpCode
	 *            is the given <tt>String</tt> to convert. "0" is a magic value,
	 *            which means 'all codes'.
	 * 
	 * @return an {@link IcmpCode} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIcmpCodeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is < -1 ;</li>
	 *             </ul>
	 */
	public static IcmpCode parseString(String icmpCode)
			throws IllegalIcmpCodeException {
		return new IcmpCode(icmpCode);
	}

	private int _value;

	public IcmpCode(int icmpCode) throws IllegalIcmpCodeException {
		setIcmpCode(icmpCode);
	}

	public IcmpCode(String icmpCode) throws IllegalIcmpCodeException {
		setIcmpCode(icmpCode);
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
	 * @return this object's value.
	 */
	public int getValue() {
		return _value;
	}

	private int setIcmpCode(int icmpCode) throws IllegalIcmpCodeException {
		if (icmpCode < -1) {
			throw new IllegalIcmpCodeException(Msg.bind(
					Messages.IcmpCodeEx_NEGATIVE, icmpCode));
		}
		int previous = getValue();
		_value = icmpCode;
		return previous;
	}

	private int setIcmpCode(String icmpCode) throws IllegalIcmpCodeException {
		if (icmpCode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IcmpCode.class.getCanonicalName() + ").");
		}
		if (icmpCode.trim().length() == 0) {
			throw new IllegalIcmpCodeException(Msg.bind(
					Messages.IcmpCodeEx_EMPTY, icmpCode));
		}
		if (icmpCode.equals(_ALL)) {
			return setIcmpCode(-1);
		}
		try {
			return setIcmpCode(Integer.parseInt(icmpCode));
		} catch (NumberFormatException Ex) {
			throw new IllegalIcmpCodeException(Msg.bind(
					Messages.IcmpCodeEx_NOT_A_NUMBER, icmpCode));
		}
	}

}
package com.wat.melody.common.network;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.exception.IllegalIpAddressException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IpAddressV4 {

	public static final String PATTERN = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])[.]"
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])[.]"
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])[.]"
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link IpAddressV4} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>A'.'B'.'C'.'D</tt>
	 * <ul>
	 * <li>A, B, C and D must be a >=0 and <256 ;</li>
	 * </ul>
	 * 
	 * @param ipAddress
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link IpAddressV4} object, which is equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIpAddressException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static IpAddressV4 parseString(String ipAddress)
			throws IllegalIpAddressException {
		return new IpAddressV4(ipAddress);
	}

	private String _value;

	public IpAddressV4(String ipAddress) throws IllegalIpAddressException {
		setValue(ipAddress);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
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
		if (anObject instanceof IpAddressV4) {
			IpAddressV4 ipAddr = (IpAddressV4) anObject;
			return getValue().equals(ipAddr.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String ipAddress) throws IllegalIpAddressException {
		if (ipAddress == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpAddressV4.class.getCanonicalName() + ").");
		}
		if (ipAddress.trim().length() == 0) {
			throw new IllegalIpAddressException(Msg.bind(
					Messages.IpAddrEx_EMPTY, ipAddress));
		} else if (!ipAddress.matches("^" + PATTERN + "$")) {
			throw new IllegalIpAddressException(Msg.bind(
					Messages.IpAddrEx_INVALID, ipAddress, PATTERN));
		}
		String previous = getValue();
		_value = ipAddress;
		return previous;
	}

}
package com.wat.melody.common.network;

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
	 * Convert the given <code>String</code> to an {@link IpAddressV4} object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Input <code>String</code> must respect the following pattern :
	 * <code>A'.'B'.'C'.'D</code> ;</li>
	 * <li>The A, B, C and D Part must be a positive integer < 256 ;</li>
	 * </ul>
	 * 
	 * @param sIpAddress
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link IpAddressV4} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalIpAddressException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link IpAddressV4}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static IpAddressV4 parseString(String sIpAddress)
			throws IllegalIpAddressException {
		return new IpAddressV4(sIpAddress);
	}

	private String msValue;

	public IpAddressV4(String sIpAddress) throws IllegalIpAddressException {
		setValue(sIpAddress);
	}

	@Override
	public String toString() {
		return msValue;
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
		return msValue;
	}

	public String setValue(String sIpAddress) throws IllegalIpAddressException {
		if (sIpAddress == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpAddressV4.class.getCanonicalName() + ").");
		}
		if (sIpAddress.trim().length() == 0) {
			throw new IllegalIpAddressException(Messages.bind(
					Messages.IpAddrEx_EMPTY, sIpAddress));
		} else if (!sIpAddress.matches("^" + PATTERN + "$")) {
			throw new IllegalIpAddressException(Messages.bind(
					Messages.IpAddrEx_INVALID, sIpAddress, PATTERN));
		}
		String previous = getValue();
		msValue = sIpAddress;
		return previous;
	}

}

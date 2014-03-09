package com.wat.melody.common.network;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.exception.IllegalIpRangeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IpRange implements Address {

	public static final String PATTERN = IpAddressV4.PATTERN
			+ "/([12]?\\d|3[0-2])";

	private static final String _ALL = "0.0.0.0/0";

	public static final IpRange ALL = createIpRange(_ALL);

	private static IpRange createIpRange(String ipRange) {
		try {
			return new IpRange(ipRange);
		} catch (IllegalIpRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "an IpRange with value '" + ipRange + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link IpRange} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>ipAddress(/CIDR)?</tt>
	 * <ul>
	 * <li><tt>ipAddress</tt> must be a valid {@link IpAddressV4}, as described
	 * in {@link IpAddressV4#parseString(String)} ;</li>
	 * <li><tt>CIDR</tt> is optional and must be a positive integer >= 0 and <=
	 * 32. When not provided, it is equals to 0 ;</li>
	 * <li>The given <tt>String</tt> can also be equal to 'all', which is equal
	 * to 0.0.0.0/0 ;</li>
	 * </ul>
	 * 
	 * @param ipRange
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link IpRange} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIpRangeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern ;</li>
	 *             </ul>
	 */
	public static IpRange parseString(String ipRange)
			throws IllegalIpRangeException {
		return new IpRange(ipRange);
	}

	private String _value;

	public IpRange(String ipRange) throws IllegalIpRangeException {
		setValue(ipRange);
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
		if (anObject instanceof IpRange) {
			IpRange ipRange = (IpRange) anObject;
			return getValue().equals(ipRange.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	public String getIp() {
		return _value.split("/")[0];
	}

	public String getMask() {
		return _value.split("/")[1];
	}

	private String setValue(String ipRange) throws IllegalIpRangeException {
		String previous = getValue();
		if (ipRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpRange.class.getCanonicalName() + ").");
		}
		if (ipRange.trim().length() == 0) {
			throw new IllegalIpRangeException(Msg.bind(
					Messages.IpRangeEx_EMPTY, ipRange));
		} else if (ipRange.equalsIgnoreCase("all")) {
			_value = _ALL;
			return previous;
		} else if (ipRange.matches("^" + IpAddressV4.PATTERN + "$")) {
			ipRange += "/32";
		}
		if (!ipRange.matches("^" + PATTERN + "$")) {
			throw new IllegalIpRangeException(Msg.bind(
					Messages.IpRangeEx_INVALID, ipRange, PATTERN));
		}
		_value = ipRange;
		return previous;
	}

	@Override
	public String getAddressAsString() {
		return getValue();
	}

}
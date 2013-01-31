package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalIpRangeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IpRange {
	public static final String PATTERN = IpAddressV4.PATTERN
			+ "/([12]?\\d|3[0-2])";

	public static final String ALL = "0.0.0.0/0";

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link IpRange} object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Input <code>String</code> must respect the following pattern :
	 * <code>IpAddress('/'CIDR)?</code> ;</li>
	 * <li>The IpAddress must be a valid {@link IpAddressV4}, as described in
	 * {@link IpAddressV4#parseString(String)} ;</li>
	 * <li>The CIDR Part must be a positive integer >0 and < 32 and is not
	 * mandatory ;</li>
	 * </ul>
	 * 
	 * @param sIpRange
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link IpRange} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalIpRangeException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link IpRange}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static IpRange parseString(String sIpRange)
			throws IllegalIpRangeException {
		return new IpRange(sIpRange);
	}

	private String msValue;

	public IpRange(String sIpRange) throws IllegalIpRangeException {
		setValue(sIpRange);
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
		if (anObject instanceof IpRange) {
			IpRange ipRange = (IpRange) anObject;
			return getValue().equals(ipRange.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String sIpRange) throws IllegalIpRangeException {
		String previous = getValue();
		if (sIpRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpRange.class.getCanonicalName() + ").");
		}
		if (sIpRange.trim().length() == 0) {
			throw new IllegalIpRangeException(Messages.bind(
					Messages.IpRangeEx_EMPTY, sIpRange));
		} else if (sIpRange.equalsIgnoreCase("all")) {
			msValue = ALL;
			return previous;
		} else if (sIpRange.matches("^" + IpAddressV4.PATTERN + "$")) {
			sIpRange += "/32";
		}
		if (!sIpRange.matches("^" + PATTERN + "$")) {
			throw new IllegalIpRangeException(Messages.bind(
					Messages.IpRangeEx_INVALID, sIpRange, PATTERN));
		}
		msValue = sIpRange;
		return previous;
	}

}

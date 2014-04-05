package com.wat.melody.common.network;

import java.util.LinkedHashSet;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalIpRangesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IpRanges extends LinkedHashSet<IpRange> {

	private static final long serialVersionUID = -2798809865423798432L;

	public static final String IP_RANGES_SEPARATOR = ",";

	public static final IpRanges ALL = new IpRanges(IpRange.ALL);

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link IpRanges} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>ipRange(,ipRange)*</tt>
	 * <ul>
	 * <li>Each <tt>ipRange</tt> must be a valid {@link IpRange} (see
	 * {@link IpRange#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param ipRanges
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link IpRanges} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIpRangesException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if an <tt>ipRange</tt> is not a valid {@link IpRange} ;</li>
	 *             </ul>
	 */
	public static IpRanges parseString(String ipRanges)
			throws IllegalIpRangesException {
		return new IpRanges(ipRanges);
	}

	public IpRanges(String ipRanges) throws IllegalIpRangesException {
		super();
		setIpRanges(ipRanges);
	}

	public IpRanges(IpRange... ipRanges) {
		super();
		setIpRanges(ipRanges);
	}

	private void setIpRanges(String ipRanges) throws IllegalIpRangesException {
		if (ipRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpRanges.class.getCanonicalName() + ").");
		}
		clear();
		for (String ipRange : ipRanges.split(IP_RANGES_SEPARATOR)) {
			ipRange = ipRange.trim();
			if (ipRange.length() == 0) {
				throw new IllegalIpRangesException(Msg.bind(
						Messages.IpRangesEx_EMPTY_IP_RANGE, ipRanges));
			}
			try {
				add(IpRange.parseString(ipRange));
			} catch (IllegalIpRangeException Ex) {
				throw new IllegalIpRangesException(Msg.bind(
						Messages.IpRangesEx_INVALID_IP_RANGE, ipRanges), Ex);
			}
		}
	}

	private void setIpRanges(IpRange... ipRanges) {
		clear();
		if (ipRanges == null) {
			return;
		}
		for (IpRange ipRange : ipRanges) {
			if (ipRange == null) {
				continue;
			} else {
				add(ipRange);
			}
		}
	}

}
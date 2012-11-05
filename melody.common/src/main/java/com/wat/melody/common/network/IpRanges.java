package com.wat.melody.common.network;

import java.util.ArrayList;

import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalIpRangesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IpRanges extends ArrayList<IpRange> {

	private static final long serialVersionUID = -2798809865423798432L;

	public static final String IP_RANGES_SEPARATOR = ",";

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link IpRanges} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>IpRange(','IpRange)*</code>. <BR/>
	 * * Each IpRange must be a valid {@link IpRange} (see
	 * {@link IpRange#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sIpRanges
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>IpRanges</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalIpRangesException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>IpRanges</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static IpRanges parseString(String sIpRanges)
			throws IllegalIpRangesException {
		return new IpRanges(sIpRanges);
	}

	public IpRanges(String sIpRanges) throws IllegalIpRangesException {
		super();
		setIpRanges(sIpRanges);
	}

	public void setIpRanges(String sIpRanges) throws IllegalIpRangesException {
		if (sIpRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IpRanges.class.getCanonicalName() + ").");
		}
		clear();
		for (String ipRange : sIpRanges.split(IP_RANGES_SEPARATOR)) {
			ipRange = ipRange.trim();
			if (ipRange.length() == 0) {
				throw new IllegalIpRangesException(Messages.bind(
						Messages.IpRangesEx_EMPTY_IP_RANGE, sIpRanges));
			}
			try {
				add(IpRange.parseString(ipRange));
			} catch (IllegalIpRangeException Ex) {
				throw new IllegalIpRangesException(Messages.bind(
						Messages.IpRangesEx_INVALID_IP_RANGE, sIpRanges), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalIpRangesException(Messages.bind(
					Messages.IpRangesEx_EMPTY, sIpRanges));
		}
	}

}

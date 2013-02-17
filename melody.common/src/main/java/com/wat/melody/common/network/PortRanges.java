package com.wat.melody.common.network;

import java.util.ArrayList;

import com.wat.melody.common.network.exception.IllegalPortRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PortRanges extends ArrayList<PortRange> {

	private static final long serialVersionUID = -4372918754817213193L;

	public static final String PORT_RANGES_SEPARATOR = ",";

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link PortRanges} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>PortRange(','PortRange)*</code>. <BR/>
	 * * Each PortRange must be a valid {@link PortRange} (see
	 * {@link PortRange#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sPortRanges
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>PortRanges</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalPortRangesException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>PortRanges</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static PortRanges parseString(String sPortRanges)
			throws IllegalPortRangesException {
		return new PortRanges(sPortRanges);
	}

	public PortRanges(String sPortRanges) throws IllegalPortRangesException {
		super();
		setPortRanges(sPortRanges);
	}

	public void setPortRanges(String sPortRanges)
			throws IllegalPortRangesException {
		toString();
		if (sPortRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PortRanges.class.getCanonicalName() + ").");
		}
		clear();
		for (String portRange : sPortRanges.split(PORT_RANGES_SEPARATOR)) {
			portRange = portRange.trim();
			if (portRange.length() == 0) {
				throw new IllegalPortRangesException(Messages.bind(
						Messages.PortRangesEx_EMPTY_PORT_RANGE, sPortRanges));
			}
			try {
				add(PortRange.parseString(portRange));
			} catch (IllegalPortRangeException Ex) {
				throw new IllegalPortRangesException(Messages.bind(
						Messages.PortRangesEx_INVALID_PORT_RANGE, sPortRanges),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalPortRangesException(Messages.bind(
					Messages.PortRangesEx_EMPTY, sPortRanges));
		}
	}

}

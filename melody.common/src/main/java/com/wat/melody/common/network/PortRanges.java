package com.wat.melody.common.network;

import java.util.LinkedHashSet;

import com.wat.melody.common.network.exception.IllegalPortRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PortRanges extends LinkedHashSet<PortRange> {

	private static final long serialVersionUID = -4372918754817213193L;

	public static final String PORT_RANGES_SEPARATOR = ",";

	public static final PortRanges ALL = createPortRanges(PortRange.ALL);

	private static PortRanges createPortRanges(PortRange... portRanges) {
		try {
			return new PortRanges(portRanges);
		} catch (IllegalPortRangesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a PortRange with value '" + portRanges + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

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
	 * @return a {@link PortRanges} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalPortRangesException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link PortRanges}.
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

	public PortRanges(PortRange... portRanges)
			throws IllegalPortRangesException {
		super();
		setPortRanges(portRanges);
	}

	public void setPortRanges(String sPortRanges)
			throws IllegalPortRangesException {
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

	public void setPortRanges(PortRange... portRanges)
			throws IllegalPortRangesException {
		clear();
		if (portRanges == null) {
			return;
		}
		for (PortRange portRange : portRanges) {
			if (portRange == null) {
				continue;
			} else {
				add(portRange);
			}
		}
		if (size() == 0) {
			throw new IllegalPortRangesException(Messages.bind(
					Messages.PortRangesEx_EMPTY, portRanges));
		}
	}

}

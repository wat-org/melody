package com.wat.melody.common.network;

import java.util.LinkedHashSet;

import com.wat.melody.common.messages.Msg;
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
	 * Convert the given <tt>String</tt> to a {@link PortRanges} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>portRange(,portRange)*</tt>
	 * <ul>
	 * <li>Each <tt>portRange</tt> must be a valid {@link PortRange} (see
	 * {@link PortRange#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param portRanges
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PortRanges} object, which is equal to the
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPortRangesException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>portRange</tt> is not a valid {@link PortRange}
	 *             ;</li>
	 *             </ul>
	 */
	public static PortRanges parseString(String portRanges)
			throws IllegalPortRangesException {
		return new PortRanges(portRanges);
	}

	public PortRanges(String portRanges) throws IllegalPortRangesException {
		super();
		setPortRanges(portRanges);
	}

	public PortRanges(PortRange... portRanges)
			throws IllegalPortRangesException {
		super();
		setPortRanges(portRanges);
	}

	private void setPortRanges(String portRanges)
			throws IllegalPortRangesException {
		if (portRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PortRanges.class.getCanonicalName() + ").");
		}
		clear();
		for (String portRange : portRanges.split(PORT_RANGES_SEPARATOR)) {
			portRange = portRange.trim();
			if (portRange.length() == 0) {
				throw new IllegalPortRangesException(Msg.bind(
						Messages.PortRangesEx_EMPTY_PORT_RANGE, portRanges));
			}
			try {
				add(PortRange.parseString(portRange));
			} catch (IllegalPortRangeException Ex) {
				throw new IllegalPortRangesException(Msg.bind(
						Messages.PortRangesEx_INVALID_PORT_RANGE, portRanges),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalPortRangesException(Msg.bind(
					Messages.PortRangesEx_EMPTY, portRanges));
		}
	}

	private void setPortRanges(PortRange... portRanges)
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
			throw new IllegalPortRangesException(Msg.bind(
					Messages.PortRangesEx_EMPTY, (Object[]) portRanges));
		}
	}

}
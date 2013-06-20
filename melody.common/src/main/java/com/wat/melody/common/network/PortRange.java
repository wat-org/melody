package com.wat.melody.common.network;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PortRange {

	public static final String PORT_SEPARATOR = "-";

	public static final PortRange ALL = createPortRange(Port.MIN, Port.MAX);

	private static PortRange createPortRange(Port start, Port end) {
		try {
			return new PortRange(start, end);
		} catch (IllegalPortRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a PortRange with value '" + start + PORT_SEPARATOR + end
					+ "'. " + "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link PortRange} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>start-end</tt>
	 * <ul>
	 * <li><tt>start</tt> and <tt>end</tt> must be valid {@link Port} (see
	 * {@link Port#parseString(String)}) ;</li>
	 * <li><tt>start</tt> cannot be lower than 'end' ;</li>
	 * <li><tt>start</tt> is optional. When not provided, it is equal to
	 * {@link Port#MIN} ;</li>
	 * <li><tt>end</tt> is optional. When not provided, it is equal to
	 * {@link Port#MAX} ;</li>
	 * <li>The given <tt>String</tt> can also be equal to 'all', which is equal
	 * to {@link Port#MIN}-{@link Port#MAX} ;</li>
	 * </ul>
	 * 
	 * @param portRange
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PortRange} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPortRangeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if <tt>start</tt> is not a valid {@link Port} ;</li>
	 *             <li>if <tt>end</tt> is not a valid {@link Port} ;</li>
	 *             <li>if <tt>start</tt> is higher then <tt>end</tt> ;</li>
	 *             </ul>
	 */
	public static PortRange parseString(String portRange)
			throws IllegalPortRangeException {
		return new PortRange(portRange);
	}

	private Port _startPort;
	private Port _endPort;

	public PortRange(String portRange) throws IllegalPortRangeException {
		setPortRange(portRange);
	}

	public PortRange(Port port) {
		setPortRange(port);
	}

	public PortRange(Port start, Port end) throws IllegalPortRangeException {
		setPortRange(start, end);
	}

	@Override
	public int hashCode() {
		return getStartPort().hashCode() + getEndPort().hashCode();
	}

	@Override
	public String toString() {
		if (getStartPort().getValue() == getEndPort().getValue()) {
			return getStartPort().toString();
		} else {
			return getStartPort() + PORT_SEPARATOR + getEndPort();
		}
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PortRange) {
			PortRange portRange = (PortRange) anObject;
			return getStartPort().equals(portRange.getStartPort())
					&& getEndPort().equals(portRange.getEndPort());
		}
		return false;
	}

	public Port getStartPort() {
		return _startPort;
	}

	public Port setStartPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getStartPort();
		_startPort = port;
		return previous;
	}

	private Port setStartPort(String port) throws IllegalPortException {
		return setStartPort(new Port(port));
	}

	public Port getEndPort() {
		return _endPort;
	}

	private Port setEndPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getEndPort();
		_endPort = port;
		return previous;
	}

	private Port setEndPort(String port) throws IllegalPortException {
		return setEndPort(new Port(port));
	}

	private void setPortRange(String portRange)
			throws IllegalPortRangeException {
		if (portRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PortRange.class.getCanonicalName() + ").");
		}
		if (portRange.trim().length() == 0) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_EMPTY, portRange));
		}

		String sStartPart = null;
		String sEndPart = null;
		int sep = portRange.indexOf(PORT_SEPARATOR);
		if (portRange.equalsIgnoreCase("all")) {
			sStartPart = String.valueOf(Port.MIN);
			sEndPart = String.valueOf(Port.MAX);
		} else if (sep == -1) {
			sStartPart = portRange;
			sEndPart = portRange;
		} else if (sep == 0 && portRange.length() == 1) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_MISSING_START_TO_PART, portRange));
		} else if (sep == 0) {
			sStartPart = String.valueOf(Port.MIN);
			sEndPart = portRange.substring(1);
		} else if (sep == portRange.length() - 1) {
			sStartPart = portRange.substring(0, portRange.length() - 1);
			sEndPart = String.valueOf(Port.MAX);
		} else {
			sStartPart = portRange.substring(0, sep);
			sEndPart = portRange.substring(sep + 1);
		}

		try {
			setStartPort(sStartPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_INVALID_START_PART, portRange), Ex);
		}
		try {
			setEndPort(sEndPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_INVALID_END_PART, portRange), Ex);
		}

		if (getEndPort().getValue() < getStartPort().getValue()) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, portRange,
					getStartPort(), getEndPort()));
		}
	}

	private void setPortRange(Port start, Port end)
			throws IllegalPortRangeException {
		if (start == null && end == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be two valid " + Port.class.getCanonicalName()
					+ ".");
		}
		if (start == null) {
			start = Port.MIN;
		}
		if (end == null) {
			end = Port.MAX;
		}

		setStartPort(start);
		setEndPort(end);

		if (getEndPort().getValue() < getStartPort().getValue()) {
			throw new IllegalPortRangeException(Msg.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, start + PORT_SEPARATOR
							+ end, start, end));
		}
	}

	private void setPortRange(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}

		setStartPort(port);
		setEndPort(port);
	}

}
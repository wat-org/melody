package com.wat.melody.common.network;

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
	 * Convert the given <code>String</code> to a {@link PortRange} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>start('-'end)?</code>. <BR/>
	 * * 'start' and 'end' must be valid {@link Port} (see
	 * {@link Port#parseString(String)}). <BR/>
	 * * 'start' cannot be lower than 'end'. <BR/>
	 * * Input <code>String</code> can also be equal to 'all', which is equal to
	 * {@link Port#MIN}-{@link Port#MAX}. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sPortRange
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link PortRange} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalPortRangeException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link PortRange}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static PortRange parseString(String sPortRange)
			throws IllegalPortRangeException {
		return new PortRange(sPortRange);
	}

	private Port moStartPort;
	private Port moEndPort;

	public PortRange(String sPortRange) throws IllegalPortRangeException {
		setPortRange(sPortRange);
	}

	public PortRange(Port start, Port end) throws IllegalPortRangeException {
		setPortRange(start, end);
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
		return moStartPort;
	}

	public Port setStartPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getStartPort();
		moStartPort = port;
		return previous;
	}

	public Port setStartPort(String sPort) throws IllegalPortException {
		return setStartPort(new Port(sPort));
	}

	public Port getEndPort() {
		return moEndPort;
	}

	public Port setEndPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getEndPort();
		moEndPort = port;
		return previous;
	}

	public Port setEndPort(String sPort) throws IllegalPortException {
		return setEndPort(new Port(sPort));
	}

	public void setPortRange(String sPortRange)
			throws IllegalPortRangeException {
		if (sPortRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PortRange.class.getCanonicalName() + ").");
		}
		if (sPortRange.trim().length() == 0) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_EMPTY, sPortRange));
		}

		String sStartPart = null;
		String sEndPart = null;
		int sep = sPortRange.indexOf(PORT_SEPARATOR);
		if (sPortRange.equalsIgnoreCase("all")) {
			sStartPart = String.valueOf(Port.MIN);
			sEndPart = String.valueOf(Port.MAX);
		} else if (sep == -1) {
			sStartPart = sPortRange;
			sEndPart = sPortRange;
		} else if (sep == 0 && sPortRange.length() == 1) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_MISSING_START_TO_PART, sPortRange));
		} else if (sep == 0) {
			sStartPart = String.valueOf(Port.MIN);
			sEndPart = sPortRange.substring(1);
		} else if (sep == sPortRange.length() - 1) {
			sStartPart = sPortRange.substring(0, sPortRange.length() - 1);
			sEndPart = String.valueOf(Port.MAX);
		} else {
			sStartPart = sPortRange.substring(0, sep);
			sEndPart = sPortRange.substring(sep + 1);
		}

		try {
			setStartPort(sStartPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_INVALID_START_PART, sPortRange), Ex);
		}
		try {
			setEndPort(sEndPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_INVALID_END_PART, sPortRange), Ex);
		}

		if (getEndPort().getValue() < getStartPort().getValue()) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, new Object[] {
							sPortRange, getStartPort(), getEndPort() }));
		}
	}

	public void setPortRange(Port start, Port end)
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
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, new Object[] {
							start + PORT_SEPARATOR + end, start, end }));
		}
	}
}

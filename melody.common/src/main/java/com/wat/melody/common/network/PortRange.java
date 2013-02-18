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

	private static PortRange createPortRange(Port from, Port to) {
		try {
			return new PortRange(from, to);
		} catch (IllegalPortRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a PortRange with value '" + from + PORT_SEPARATOR + to
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
	 * <code>From('-'To)?</code>. <BR/>
	 * * From and To must be valid {@link Port} (see
	 * {@link Port#parseString(String)}). <BR/>
	 * * From cannot be lower than To. <BR/>
	 * * Input <code>String</code> can also be equal to 'all', which is equal to
	 * {@link Port#MIN}-{@link Port#MAX}. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sPortRange
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>PortRange</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalPortRangeException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>PortRange</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static PortRange parseString(String sPortRange)
			throws IllegalPortRangeException {
		return new PortRange(sPortRange);
	}

	private Port msFromPort;
	private Port msToPort;

	public PortRange(String sPortRange) throws IllegalPortRangeException {
		setPortRange(sPortRange);
	}

	public PortRange(Port from, Port to) throws IllegalPortRangeException {
		setPortRange(from, to);
	}

	@Override
	public String toString() {
		if (getFromPort().getValue() == getToPort().getValue()) {
			return getFromPort().toString();
		} else {
			return getFromPort() + PORT_SEPARATOR + getToPort();
		}
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PortRange) {
			PortRange portRange = (PortRange) anObject;
			return getFromPort().equals(portRange.getFromPort())
					&& getToPort().equals(portRange.getToPort());
		}
		return false;
	}

	public Port getFromPort() {
		return msFromPort;
	}

	public Port setFromPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getFromPort();
		msFromPort = port;
		return previous;
	}

	public Port setFromPort(String sPort) throws IllegalPortException {
		return setFromPort(new Port(sPort));
	}

	public Port getToPort() {
		return msToPort;
	}

	public Port setToPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getToPort();
		msToPort = port;
		return previous;
	}

	public Port setToPort(String sPort) throws IllegalPortException {
		return setToPort(new Port(sPort));
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

		String sFromPart = null;
		String sToPart = null;
		int sep = sPortRange.indexOf(PORT_SEPARATOR);
		if (sPortRange.equalsIgnoreCase("all")) {
			sFromPart = String.valueOf(Port.MIN);
			sToPart = String.valueOf(Port.MAX);
		} else if (sep == -1) {
			sFromPart = sPortRange;
			sToPart = sPortRange;
		} else if (sep == 0 && sPortRange.length() == 1) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_MISSING_FROM_TO_PART, sPortRange));
		} else if (sep == 0) {
			sFromPart = String.valueOf(Port.MIN);
			sToPart = sPortRange.substring(1);
		} else if (sep == sPortRange.length() - 1) {
			sFromPart = sPortRange.substring(0, sPortRange.length() - 1);
			sToPart = String.valueOf(Port.MAX);
		} else {
			sFromPart = sPortRange.substring(0, sep);
			sToPart = sPortRange.substring(sep + 1);
		}

		try {
			setFromPort(sFromPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_INVALID_FROM_PART, sPortRange), Ex);
		}
		try {
			setToPort(sToPart);
		} catch (IllegalPortException Ex) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_INVALID_TO_PART, sPortRange), Ex);
		}

		if (getToPort().getValue() < getFromPort().getValue()) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, new Object[] {
							sPortRange, getFromPort(), getToPort() }));
		}
	}

	public void setPortRange(Port from, Port to)
			throws IllegalPortRangeException {
		if (from == null && to == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be two valid " + Port.class.getCanonicalName()
					+ ".");
		}
		if (from == null) {
			from = Port.MIN;
		}
		if (to == null) {
			to = Port.MAX;
		}

		setFromPort(from);
		setToPort(to);

		if (getToPort().getValue() < getFromPort().getValue()) {
			throw new IllegalPortRangeException(Messages.bind(
					Messages.PortRangeEx_ILLOGIC_RANGE, new Object[] {
							from + PORT_SEPARATOR + to, from, to }));
		}
	}
}

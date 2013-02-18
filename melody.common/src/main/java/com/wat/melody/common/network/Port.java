package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalPortException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Port {

	private static final int _MIN = 1;
	private static final int _MAX = 65535;

	public static final Port MIN = createPort(_MIN);
	public static final Port MAX = createPort(_MAX);
	public static final Port SSH = createPort(22);
	public static final Port HTTP = createPort(80);
	public static final Port HTTPS = createPort(443);

	private static Port createPort(int port) {
		try {
			return new Port(port);
		} catch (IllegalPortException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Port with value '" + port + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link Port} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must be an Integer, higher or equal to
	 * <code>MIN_PORT</code> and lower or equal to <code>MAX_PORT</code>. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sPort
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>Port</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalPortException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>Port</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Port parseString(String sPort) throws IllegalPortException {
		return new Port(sPort);
	}

	private int miValue;

	public Port(String sPort) throws IllegalPortException {
		setValue(sPort);
	}

	public Port(int iPort) throws IllegalPortException {
		setValue(iPort);
	}

	@Override
	public String toString() {
		return String.valueOf(miValue);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof Port) {
			Port ipRange = (Port) anObject;
			return getValue() == ipRange.getValue();
		}
		return false;
	}

	public int getValue() {
		return miValue;
	}

	public int setValue(int iPort) throws IllegalPortException {
		if (iPort < _MIN) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_LOW,
					new Object[] { iPort, _MIN, _MAX }));
		} else if (iPort > _MAX) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_HIGH,
					new Object[] { iPort, _MIN, _MAX }));
		}
		int previous = getValue();
		miValue = iPort;
		return previous;
	}

	public int setValue(String sPort) throws IllegalPortException {
		if (sPort == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Port.class.getCanonicalName() + ").");
		}
		if (sPort.trim().length() == 0) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_EMPTY,
					sPort));
		}
		int iPort = 0;
		try {
			iPort = Integer.parseInt(sPort);
		} catch (NumberFormatException Ex) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_NAN,
					new Object[] { sPort, _MIN, _MAX }));
		}
		return setValue(iPort);
	}

}

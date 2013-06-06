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
	// TODO : find the port of winrn
	public static final Port WINRM = createPort(8888);

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
	 * Convert the given <tt>int</tt> to a {@link Port} object.
	 * </p>
	 * 
	 * @param sPort
	 *            is the given <tt>int</tt> to convert.
	 * 
	 * @return a {@link Port} object, whose equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalPortException
	 *             if the given <tt>int</tt> is lower than {@link #MIN}.
	 * @throws IllegalPortException
	 *             if the given <tt>int</tt> is higher than {@link #MAX}.
	 */
	public static Port parseInt(int iPort) throws IllegalPortException {
		return new Port(iPort);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Port} object.
	 * </p>
	 * 
	 * @param sPort
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Port} object, whose equal to the given <tt>String</tt>.
	 * 
	 * @throws IllegalPortException
	 *             if the given <tt>String</tt> is not an <tt>Integer</tt>.
	 * @throws IllegalPortException
	 *             if the given <tt>String</tt> is lower than {@link #MIN}.
	 * @throws IllegalPortException
	 *             if the given <tt>String</tt> is higher than {@link #MAX}.
	 * @throws IllegalPortException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static Port parseString(String sPort) throws IllegalPortException {
		return new Port(sPort);
	}

	private int _value;

	public Port(String sPort) throws IllegalPortException {
		setValue(sPort);
	}

	public Port(int iPort) throws IllegalPortException {
		setValue(iPort);
	}

	@Override
	public int hashCode() {
		return getValue();
	}

	@Override
	public String toString() {
		return String.valueOf(_value);
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
		return _value;
	}

	private int setValue(int iPort) throws IllegalPortException {
		if (iPort < _MIN) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_LOW,
					new Object[] { iPort, _MIN, _MAX }));
		} else if (iPort > _MAX) {
			throw new IllegalPortException(Messages.bind(Messages.PortEx_HIGH,
					new Object[] { iPort, _MIN, _MAX }));
		}
		int previous = getValue();
		_value = iPort;
		return previous;
	}

	private int setValue(String sPort) throws IllegalPortException {
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
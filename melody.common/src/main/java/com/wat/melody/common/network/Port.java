package com.wat.melody.common.network;

import com.wat.melody.common.messages.Msg;
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
	 * @return a {@link Port} object, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalPortException
	 *             <ul>
	 *             <li>if the given <tt>int</tt> is lower than {@link #MIN} ;</li>
	 *             <li>if the given <tt>int</tt> is higher than {@link #MAX} ;</li>
	 *             </ul>
	 */
	public static Port parseInt(int port) throws IllegalPortException {
		return new Port(port);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Port} object.
	 * </p>
	 * 
	 * @param port
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Port} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPortException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Integer</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is lower than {@link #MIN} ;
	 *             </li>
	 *             <li>if the given <tt>String</tt> is higher than {@link #MAX}
	 *             ;</li>
	 *             </ul>
	 */
	public static Port parseString(String port) throws IllegalPortException {
		return new Port(port);
	}

	private int _value;

	public Port(String port) throws IllegalPortException {
		setValue(port);
	}

	public Port(int port) throws IllegalPortException {
		setValue(port);
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

	private int setValue(int port) throws IllegalPortException {
		if (port < _MIN) {
			throw new IllegalPortException(Msg.bind(Messages.PortEx_LOW, port,
					_MIN, _MAX));
		} else if (port > _MAX) {
			throw new IllegalPortException(Msg.bind(Messages.PortEx_HIGH, port,
					_MIN, _MAX));
		}
		int previous = getValue();
		_value = port;
		return previous;
	}

	private int setValue(String port) throws IllegalPortException {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Port.class.getCanonicalName() + ").");
		}
		if (port.trim().length() == 0) {
			throw new IllegalPortException(
					Msg.bind(Messages.PortEx_EMPTY, port));
		}
		int iPort = 0;
		try {
			iPort = Integer.parseInt(port);
		} catch (NumberFormatException Ex) {
			throw new IllegalPortException(Msg.bind(Messages.PortEx_NAN, port,
					_MIN, _MAX));
		}
		return setValue(iPort);
	}

}
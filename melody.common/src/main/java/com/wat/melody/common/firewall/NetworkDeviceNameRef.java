package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceNameRef {

	public static final String PATTERN = ".*";

	private static final String _ALL = "all";

	public static final NetworkDeviceNameRef ALL = createNetworkDeviceRef(_ALL);

	private static NetworkDeviceNameRef createNetworkDeviceRef(
			String netDevNameRef) {
		try {
			return new NetworkDeviceNameRef(netDevNameRef);
		} catch (IllegalNetworkDeviceNameRefException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a NetworkDeviceNameRef with value '" + netDevNameRef
					+ "'. " + "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkDeviceNameRef}
	 * object.
	 * </p>
	 * 
	 * <p>
	 * The given <tt>String</tt> can be equal to 'all', which means 'all
	 * devices'.
	 * </p>
	 * 
	 * @param devnameRef
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link NetworkDeviceNameRef} object, whose equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalNetworkDeviceNameRefException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> neither match the pattern
	 *             {@link #PATTERN} nor equals to 'all' ;</li>
	 *             </ul>
	 */
	public static NetworkDeviceNameRef parseString(String devnameRef)
			throws IllegalNetworkDeviceNameRefException {
		return new NetworkDeviceNameRef(devnameRef);
	}

	public static NetworkDeviceNameRef fromNetworkDeviceName(
			NetworkDeviceName devnameRef) {
		try {
			return new NetworkDeviceNameRef(devnameRef.getValue());
		} catch (IllegalNetworkDeviceNameRefException e) {
			throw new RuntimeException("Unexecpted error while creating a "
					+ "NetworkDeviceNameRef from a NetworkDeviceName "
					+ "equals to '" + devnameRef + "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
	}

	private String _value;

	public NetworkDeviceNameRef(String devnameRef)
			throws IllegalNetworkDeviceNameRefException {
		setValue(devnameRef);
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public String toString() {
		return _value;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof NetworkDeviceNameRef) {
			NetworkDeviceNameRef ref = (NetworkDeviceNameRef) anObject;
			return getValue().equals(ref.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String devnameRef)
			throws IllegalNetworkDeviceNameRefException {
		String previous = toString();
		if (devnameRef == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ NetworkDeviceNameRef.class.getCanonicalName() + ").");
		}
		if (devnameRef.trim().length() == 0) {
			throw new IllegalNetworkDeviceNameRefException(Msg.bind(
					Messages.NetworkDeviceNameRefEx_EMPTY, devnameRef));
		} else if (devnameRef.equalsIgnoreCase(_ALL)) {
			_value = _ALL;
			return previous;
		} else if (!devnameRef.matches("^" + PATTERN + "$")) {
			throw new IllegalNetworkDeviceNameRefException(Msg.bind(
					Messages.NetworkDeviceNameRefEx_INVALID, devnameRef,
					PATTERN));
		}
		_value = devnameRef;
		return previous;
	}

}
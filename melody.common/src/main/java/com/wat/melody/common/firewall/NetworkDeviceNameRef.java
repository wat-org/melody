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
	 * Convert the given <code>String</code> to an {@link NetworkDeviceNameRef}
	 * object.
	 * </p>
	 * 
	 * @param netDevNameRef
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link NetworkDeviceNameRef} object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * 
	 * @throws IllegalNetworkDeviceNameRefException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link NetworkDeviceNameRef}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static NetworkDeviceNameRef parseString(String netDevNameRef)
			throws IllegalNetworkDeviceNameRefException {
		return new NetworkDeviceNameRef(netDevNameRef);
	}

	public static NetworkDeviceNameRef fromNetworkDeviceName(
			NetworkDeviceName netDevName) {
		try {
			return new NetworkDeviceNameRef(netDevName.getValue());
		} catch (IllegalNetworkDeviceNameRefException e) {
			throw new RuntimeException("Unexecpted error while creating a "
					+ "NetworkDeviceNameRef from a NetworkDeviceName "
					+ "equals to '" + netDevName + "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
	}

	private String _value;

	public NetworkDeviceNameRef(String netDevNameRef)
			throws IllegalNetworkDeviceNameRefException {
		setValue(netDevNameRef);
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

	public String setValue(String netDevNameRef)
			throws IllegalNetworkDeviceNameRefException {
		String previous = toString();
		if (netDevNameRef == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ NetworkDeviceNameRef.class.getCanonicalName() + ").");
		}
		if (netDevNameRef.trim().length() == 0) {
			throw new IllegalNetworkDeviceNameRefException(Msg.bind(
					Messages.NetworkDeviceNameRefEx_EMPTY, netDevNameRef));
		} else if (netDevNameRef.equalsIgnoreCase(_ALL)) {
			_value = _ALL;
			return previous;
		} else if (!netDevNameRef.matches("^" + PATTERN + "$")) {
			throw new IllegalNetworkDeviceNameRefException(Msg.bind(
					Messages.NetworkDeviceNameRefEx_INVALID, netDevNameRef,
					PATTERN));
		}
		_value = netDevNameRef;
		return previous;
	}

}
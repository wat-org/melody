package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceName {

	public static final String NETWORK_DEVICE_NAME_PATTERN = "eth[0-9]";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkDeviceName} object.
	 * </p>
	 * 
	 * @param name
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link NetworkDeviceName} object, which is equal to the given
	 *         name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalNetworkDeviceNameException
	 *             <ul>
	 *             <li>if the given name is empty ;</li>
	 *             <li>if the given name doesn't match the pattern
	 *             {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 *             </ul>
	 */
	public static NetworkDeviceName parseString(String name)
			throws IllegalNetworkDeviceNameException {
		return new NetworkDeviceName(name);
	}

	private String _value;

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkDeviceName} object.
	 * </p>
	 * 
	 * @param name
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalNetworkDeviceNameException
	 *             <ul>
	 *             <li>if the given name is empty ;</li>
	 *             <li>if the given name doesn't match the pattern
	 *             {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 *             </ul>
	 */
	public NetworkDeviceName(String name)
			throws IllegalNetworkDeviceNameException {
		setValue(name);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	@Override
	public String toString() {
		return getValue();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof NetworkDeviceName) {
			NetworkDeviceName d = (NetworkDeviceName) anObject;
			return getValue().equals(d.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String name)
			throws IllegalNetworkDeviceNameException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a Network Device name)");
		}
		if (name.trim().length() == 0) {
			throw new IllegalNetworkDeviceNameException(Msg.bind(
					Messages.NetworkDeviceNameEx_EMPTY, name));
		}
		if (!name.matches("^" + NETWORK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalNetworkDeviceNameException(Msg.bind(
					Messages.NetworkDeviceNameEx_INVALID, name,
					NETWORK_DEVICE_NAME_PATTERN));
		}
		String previous = getValue();
		_value = name;
		return previous;
	}

}
package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceName {

	public static final String NETWORK_DEVICE_NAME_PATTERN = "eth[0-9]";

	/**
	 * <p>
	 * create a new {@link NetworkDeviceName}, with the given name.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @throws IllegalNetworkDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
	 */
	public static NetworkDeviceName parseString(String name)
			throws IllegalNetworkDeviceNameException {
		return new NetworkDeviceName(name);
	}

	private String _value;

	/**
	 * <p>
	 * create a new {@link NetworkDeviceName}, with the given name.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @throws IllegalNetworkDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
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

	/**
	 * <p>
	 * Set the name of this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @return the name of this object, before this operation.
	 * 
	 * @throws IllegalNetworkDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
	 */
	private String setValue(String name)
			throws IllegalNetworkDeviceNameException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Network Device name)");
		}
		if (name.trim().length() == 0) {
			throw new IllegalNetworkDeviceNameException(Messages.bind(
					Messages.NetworkDeviceNameEx_EMPTY, name));
		}
		if (!name.matches("^" + NETWORK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalNetworkDeviceNameException(Messages.bind(
					Messages.NetworkDeviceNameEx_INVALID, name,
					NETWORK_DEVICE_NAME_PATTERN));
		}
		String previous = getValue();
		_value = name;
		return previous;
	}

}

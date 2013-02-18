package com.wat.melody.cloud.network;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceName {

	public static final String NETWORK_DEVICE_NAME_PATTERN = "eth[0-9]";

	public static NetworkDeviceName parseString(String sInterface)
			throws IllegalNetworkDeviceException {
		return new NetworkDeviceName(sInterface);
	}

	private String msValue;

	public NetworkDeviceName() {
		initValue();
	}

	public NetworkDeviceName(String sDeviceName)
			throws IllegalNetworkDeviceException {
		setValue(sDeviceName);
	}

	private void initValue() {
		msValue = null;
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
		return msValue;
	}

	/**
	 * <p>
	 * Set the network device name of this object.
	 * </p>
	 * <ul>
	 * <li>The given network device name should match the pattern
	 * {@link #NETWORK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDeviceName
	 *            is the network device name to assign to this object.
	 * 
	 * @return the network device name, before this operation.
	 * 
	 * @throws IllegalNetworkDeviceException
	 *             if the given network device name is invalid.
	 */
	private String setValue(String sDeviceName)
			throws IllegalNetworkDeviceException {
		if (sDeviceName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Network Device name)");
		}
		if (sDeviceName.trim().length() == 0) {
			throw new IllegalNetworkDeviceException(Messages.bind(
					Messages.NetworkDeviceNameEx_EMPTY, sDeviceName));
		}
		if (!sDeviceName.matches("^" + NETWORK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalNetworkDeviceException(Messages.bind(
					Messages.NetworkDeviceNameEx_INVALID, sDeviceName,
					NETWORK_DEVICE_NAME_PATTERN));
		}
		String previous = getValue();
		msValue = sDeviceName;
		return previous;
	}

}

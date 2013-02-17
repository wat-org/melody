package com.wat.melody.cloud.network;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevice {

	public static final String NETWORK_DEVICE_NAME_PATTERN = "eth[0-9]";

	private String msDeviceName;

	public NetworkDevice() {
		initDeviceName();
	}

	private void initDeviceName() {
		msDeviceName = null;
	}

	@Override
	public String toString() {
		return "{ " + "name:" + getDeviceName() + " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof NetworkDevice) {
			NetworkDevice d = (NetworkDevice) anObject;
			return getDeviceName().equals(d.getDeviceName());
		}
		return false;
	}

	public String getDeviceName() {
		return msDeviceName;
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
	public String setDeviceName(String sDeviceName)
			throws IllegalNetworkDeviceException {
		if (sDeviceName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Network Device name)");
		}
		if (sDeviceName.trim().length() == 0) {
			throw new IllegalNetworkDeviceException(Messages.bind(
					Messages.NetworkEx_EMPTY_DEVICE_NAME, sDeviceName));
		}
		if (!sDeviceName.matches("^" + NETWORK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalNetworkDeviceException(Messages.bind(
					Messages.NetworkEx_INVALID_DEVICE_NAME, sDeviceName,
					NETWORK_DEVICE_NAME_PATTERN));
		}
		String previous = getDeviceName();
		msDeviceName = sDeviceName;
		return previous;
	}

}

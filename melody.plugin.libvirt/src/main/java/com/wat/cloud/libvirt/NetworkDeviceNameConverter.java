package com.wat.cloud.libvirt;

import com.wat.melody.cloud.network.NetworkDeviceName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkDeviceNameConverter {

	/**
	 * <p>
	 * Converts the given {@link NetworkDeviceName} to the libvirt index of the
	 * network device.
	 * </p>
	 * 
	 * @param netdev
	 *            is the {@link NetworkDeviceName} to convert.
	 * 
	 * @return the libvirt index of the network device.
	 */
	public static int convert(NetworkDeviceName netdev) {
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		return Integer.parseInt(netdev.getValue().substring(3)) + 1;
	}

}

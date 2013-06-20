package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceName {

	public static final String PATTERN = "eth[0-9]";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkDeviceName} object.
	 * </p>
	 * 
	 * @param devname
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link NetworkDeviceName} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalNetworkDeviceNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static NetworkDeviceName parseString(String devname)
			throws IllegalNetworkDeviceNameException {
		return new NetworkDeviceName(devname);
	}

	private String _value;

	public NetworkDeviceName(String devname)
			throws IllegalNetworkDeviceNameException {
		setValue(devname);
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

	private String setValue(String devname)
			throws IllegalNetworkDeviceNameException {
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a Network Device name)");
		}
		if (devname.trim().length() == 0) {
			throw new IllegalNetworkDeviceNameException(Msg.bind(
					Messages.NetworkDeviceNameEx_EMPTY, devname));
		}
		if (!devname.matches("^" + PATTERN + "$")) {
			throw new IllegalNetworkDeviceNameException(Msg.bind(
					Messages.NetworkDeviceNameEx_INVALID, devname, PATTERN));
		}
		String previous = getValue();
		_value = devname;
		return previous;
	}

}
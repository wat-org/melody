package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefException;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefsException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceNameRefs extends LinkedHashSet<NetworkDeviceNameRef> {

	private static final long serialVersionUID = -534567888987653292L;

	public static final String SEPARATOR = ",";

	public static final NetworkDeviceNameRefs ALL = createNetworkDeiceNameRefs(NetworkDeviceNameRef.ALL);

	private static NetworkDeviceNameRefs createNetworkDeiceNameRefs(
			NetworkDeviceNameRef... refs) {
		try {
			return new NetworkDeviceNameRefs(refs);
		} catch (IllegalNetworkDeviceNameRefsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a NetworkDeviceNameRefs with value '" + refs + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkDeviceNameRefs}
	 * object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>ref(','ref)*</tt>
	 * <ul>
	 * <li>Each <tt>ref</tt> must be a valid {@link NetworkDeviceNameRef} (see
	 * {@link NetworkDeviceNameRef#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param devnameRefs
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link NetworkDeviceNameRefs} object, whose equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalNetworkDeviceNameRefsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>ref</tt> is not a valid
	 *             {@link NetworkDeviceNameRef} ;</li>
	 *             </ul>
	 */
	public static NetworkDeviceNameRefs parseString(String devnameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		return new NetworkDeviceNameRefs(devnameRefs);
	}

	public NetworkDeviceNameRefs(String devnameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		super();
		setValue(devnameRefs);
	}

	public NetworkDeviceNameRefs(NetworkDeviceNameRef... devnameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		super();
		setValue(devnameRefs);
	}

	private void setValue(NetworkDeviceNameRef... devnameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		clear();
		if (devnameRefs == null) {
			return;
		}
		for (NetworkDeviceNameRef ref : devnameRefs) {
			if (ref == null) {
				continue;
			} else {
				add(ref);
			}
		}
		if (size() == 0) {
			throw new IllegalNetworkDeviceNameRefsException(Msg.bind(
					Messages.NetworkDeviceNameRefsEx_EMPTY,
					(Object[]) devnameRefs));
		}
	}

	private void setValue(String devnameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		if (devnameRefs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ").");
		}
		clear();
		for (String ref : devnameRefs.split(SEPARATOR)) {
			ref = ref.trim();
			if (ref.length() == 0) {
				throw new IllegalNetworkDeviceNameRefsException(Msg.bind(
						Messages.NetworkDeviceNameRefsEx_EMPTY_PART,
						devnameRefs));
			}
			try {
				add(NetworkDeviceNameRef.parseString(ref));
			} catch (IllegalNetworkDeviceNameRefException Ex) {
				throw new IllegalNetworkDeviceNameRefsException(Msg.bind(
						Messages.NetworkDeviceNameRefsEx_INVALID_PART,
						devnameRefs), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalNetworkDeviceNameRefsException(Msg.bind(
					Messages.NetworkDeviceNameRefsEx_EMPTY, devnameRefs));
		}
	}

}
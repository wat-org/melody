package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefException;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefsException;

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
	 * Convert the given <code>String</code> to an {@link NetworkDeviceNameRefs}
	 * object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Input <code>String</code> must respect the following pattern :
	 * <code>Ref(','Ref)*</code> ;</li>
	 * <li>Ref must be a valid {@link NetworkDeviceNameRef} (see
	 * {@link NetworkDeviceNameRef#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param netDevNameRefs
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link NetworkDeviceNameRefs} object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalNetworkDeviceNameRefsException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link NetworkDeviceNameRefs}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static NetworkDeviceNameRefs parseString(String netDevNameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		return new NetworkDeviceNameRefs(netDevNameRefs);
	}

	public NetworkDeviceNameRefs(String netDevNameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		super();
		setValue(netDevNameRefs);
	}

	public NetworkDeviceNameRefs(NetworkDeviceNameRef... netDevNameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		super();
		setValue(netDevNameRefs);
	}

	public void setValue(NetworkDeviceNameRef... netDevNameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		clear();
		if (netDevNameRefs == null) {
			return;
		}
		for (NetworkDeviceNameRef ref : netDevNameRefs) {
			if (ref == null) {
				continue;
			} else {
				add(ref);
			}
		}
		if (size() == 0) {
			throw new IllegalNetworkDeviceNameRefsException(Messages.bind(
					Messages.NetworkDeviceNameRefsEx_EMPTY, netDevNameRefs));
		}
	}

	public void setValue(String netDevNameRefs)
			throws IllegalNetworkDeviceNameRefsException {
		if (netDevNameRefs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ").");
		}
		clear();
		for (String ref : netDevNameRefs.split(SEPARATOR)) {
			ref = ref.trim();
			if (ref.length() == 0) {
				throw new IllegalNetworkDeviceNameRefsException(Messages.bind(
						Messages.NetworkDeviceNameRefsEx_EMPTY_PART,
						netDevNameRefs));
			}
			try {
				add(NetworkDeviceNameRef.parseString(ref));
			} catch (IllegalNetworkDeviceNameRefException Ex) {
				throw new IllegalNetworkDeviceNameRefsException(Messages.bind(
						Messages.NetworkDeviceNameRefsEx_INVALID_PART,
						netDevNameRefs), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalNetworkDeviceNameRefsException(Messages.bind(
					Messages.NetworkDeviceNameRefsEx_EMPTY, netDevNameRefs));
		}
	}

}
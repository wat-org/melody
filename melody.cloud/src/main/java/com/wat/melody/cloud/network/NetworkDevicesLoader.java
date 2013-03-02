package com.wat.melody.cloud.network;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevicesLoader {

	/**
	 * XML Nested element of an Instance Node, which contains the definition of
	 * a Network Device.
	 */
	public static final String INTERFACE_NE = "interface";

	/**
	 * XML attribute of a Network Device Node, which define the name of the
	 * device.
	 */
	public static final String DEVICE_ATTR = "device";

	/**
	 * XML attribute of a Network Device Node, which define the ip of the
	 * device.
	 */
	public static final String IP_ATTR = "ip";

	/**
	 * XML attribute of a Network Device Node, which define the fqdn of the
	 * device.
	 */
	public static final String FQDN_ATTR = "fqdn";

	/**
	 * XML attribute of a Network Device Node, which define the nat-ip of the
	 * device.
	 */
	public static final String NAT_IP_ATTR = "ip";

	/**
	 * XML attribute of a Network Device Node, which define the nat-fqdn of the
	 * device.
	 */
	public static final String NAT_FQDN_ATTR = "fqdn";

	private ITaskContext moTC;

	public NetworkDevicesLoader(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moTC = tc;
	}

	protected ITaskContext getTC() {
		return moTC;
	}

	private NetworkDeviceName loadDeviceName(Node n)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(DEVICE_ATTR);
		if (attr == null) {
			throw new ResourcesDescriptorException(n, Messages.bind(
					Messages.NetworkLoadEx_MISSING_ATTR, DEVICE_ATTR));
		}
		String v = attr.getNodeValue();
		try {
			return NetworkDeviceName.parseString(v);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Converts the given Network Device {@link Node}s into a
	 * {@link NetworkDeviceNameList}.
	 * </p>
	 * 
	 * <p>
	 * A Network Device {@link Node} must have the attributes : <BR/>
	 * <ul>
	 * <li>device : which should contains a LINUX DEVICE NAME (ex : eth0, eth4)
	 * ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param nl
	 *            a list of Network Device {@link Node}s.
	 * 
	 * @return a {@link NetworkDeviceNameList} object, which is a collection of
	 *         {@link NetworkDeviceName} .
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Network
	 *             Device Node is not valid, multiple device declare with the
	 *             same name).
	 */
	public NetworkDeviceNameList load(NodeList nl)
			throws ResourcesDescriptorException {
		NetworkDeviceNameList dl = new NetworkDeviceNameList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			NetworkDeviceName netDevName = loadDeviceName(n);
			try {
				dl.addNetworkDevice(netDevName);
			} catch (IllegalNetworkDeviceNameListException Ex) {
				throw new ResourcesDescriptorException(n, "This Network "
						+ "Device Node description is not valid. Read message "
						+ "bellow to get more details about this issue.", Ex);
			}
		}
		return dl;
	}

}
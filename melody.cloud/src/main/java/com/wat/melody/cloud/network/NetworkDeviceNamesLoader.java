package com.wat.melody.cloud.network;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceNamesLoader {

	/**
	 * XML Nested element of an Instance Node, which contains the definition of
	 * a Network Device.
	 */
	public static final String INTERFACE_NE = "interface";

	/**
	 * XML attribute of a Network Device Node, which define the device name of
	 * the interface.
	 */
	public static final String DEVICE_NAME_ATTR = "device-name";

	/**
	 * XML attribute of a Network Device Node, which define the ip associated to
	 * the interface.
	 */
	public static final String IP_ATTR = "ip";

	/**
	 * XML attribute of a Network Device Node, which define the fqdn associated
	 * to the interface.
	 */
	public static final String FQDN_ATTR = "fqdn";

	/**
	 * XML attribute of a Network Device Node, which define the nat-ip
	 * associated to the interface.
	 */
	public static final String NAT_IP_ATTR = "nat-ip";

	/**
	 * XML attribute of a Network Device Node, which define the nat-fqdn
	 * associated to the interface.
	 */
	public static final String NAT_FQDN_ATTR = "nat-fqdn";

	public NetworkDeviceNamesLoader() {
	}

	private NetworkDeviceName loadDeviceName(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DEVICE_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return NetworkDeviceName.parseString(v);
		} catch (IllegalNetworkDeviceNameException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					DEVICE_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the Network Device {@link Node}s of the given Instance {@link Node}
	 * and convert it into a {@link NetworkDeviceNameList}.
	 * </p>
	 * 
	 * <p>
	 * A Network Device {@link Node} must have the attributes : <BR/>
	 * <ul>
	 * <li>device-name : which must contains a {@link NetworkDeviceName} ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return a {@link NetworkDeviceNameList} object, which is a collection of
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code> or is
	 *             not an element {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Network
	 *             Device {@link Node} is not valid, multiple Network Device
	 *             Name declare with the same name).
	 */
	public NetworkDeviceNameList load(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = NetworkManagementHelper.findNetworkDeviceNodeByName(
				instanceNode, null);

		NetworkDeviceNameList dl = new NetworkDeviceNameList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			NetworkDeviceName netDevName = loadDeviceName(n);
			if (netDevName == null) {
				throw new ResourcesDescriptorException(n,
						Messages.bind(Messages.NetworkDeviceEx_MISSING_ATTR,
								DEVICE_NAME_ATTR));
			}

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
package com.wat.melody.cloud.network;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevicesLoader {

	/**
	 * XML Nested element of an Instance Element Node, which contains the
	 * definition of a Network Device.
	 */
	public static final String INTERFACE_NE = "interface";

	/**
	 * XML attribute of a Network Device Element Node, which define the device
	 * name of the interface.
	 */
	public static final String DEVICE_NAME_ATTR = "device-name";

	/**
	 * XML attribute of a Network Device Element Node, which define the mac
	 * associated to the interface.
	 */
	public static final String MAC_ATTR = "mac";

	/**
	 * XML attribute of a Network Device Element Node, which define the ip
	 * associated to the interface.
	 */
	public static final String IP_ATTR = "ip";

	/**
	 * XML attribute of a Network Device Element Node, which define the fqdn
	 * associated to the interface.
	 */
	public static final String FQDN_ATTR = "fqdn";

	/**
	 * XML attribute of a Network Device Element Node, which define the nat-ip
	 * associated to the interface.
	 */
	public static final String NAT_IP_ATTR = "nat-ip";

	/**
	 * XML attribute of a Network Device Element Node, which define the nat-fqdn
	 * associated to the interface.
	 */
	public static final String NAT_FQDN_ATTR = "nat-fqdn";

	/**
	 * XML attribute of a Network Device Element Node, which indicate if the
	 * timeout of the device attachment operation.
	 */
	public static final String TIMEOUT_ATTACH_ATTR = "timeout-attach";

	/**
	 * XML attribute of a Network Device Element Node, which indicate if the
	 * timeout of the device detachment operation.
	 */
	public static final String TIMEOUT_DETACH_ATTR = "timeout-detach";

	public NetworkDevicesLoader() {
	}

	private NetworkDeviceName loadDeviceName(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, DEVICE_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return NetworkDeviceName.parseString(v);
		} catch (IllegalNetworkDeviceNameException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					DEVICE_NAME_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private String loadMac(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, MAC_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadIp(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, IP_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadFqdn(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, FQDN_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadNatIp(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, NAT_IP_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadNatFqdn(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, NAT_FQDN_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private GenericTimeout loadAttachTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_ATTACH_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_ATTACH_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadDetachTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_DETACH_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_DETACH_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the Network Device {@link Element}s of the given Instance
	 * {@link Element} and convert it into a {@link NetworkDeviceList}.
	 * </p>
	 * 
	 * <p>
	 * A Network Device {@link Element} must have the attributes : <BR/>
	 * <ul>
	 * <li>device-name : which must contains a {@link NetworkDeviceName} ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an Instance {@link Element}.
	 * 
	 * @return a {@link NetworkDeviceList} object, which is a collection of
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <code>null</code>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a Network
	 *             Device {@link Element} is not valid, multiple Network Device
	 *             Name declare with the same name).
	 */
	public NetworkDeviceList load(Element instanceElmt)
			throws NodeRelatedException {
		NodeList nl = NetworkManagementHelper.findNetworkDeviceNodeByName(
				instanceElmt, null);

		NetworkDeviceList dl = new NetworkDeviceList();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			NetworkDeviceName netDevName = loadDeviceName(e);
			if (netDevName == null) {
				throw new NodeRelatedException(e, Messages.bind(
						Messages.NetworkDevLoaderEx_MISSING_ATTR,
						DEVICE_NAME_ATTR));
			}
			String mac = loadMac(e);
			String ip = loadIp(e);
			String fqdn = loadFqdn(e);
			String natip = loadNatIp(e);
			String natfqdn = loadNatFqdn(e);
			GenericTimeout attachTimeout = loadAttachTimeout(e);
			GenericTimeout detachTimeout = loadDetachTimeout(e);

			try {
				dl.addNetworkDevice(new NetworkDevice(netDevName, mac, ip,
						fqdn, natip, natfqdn, attachTimeout, detachTimeout));
			} catch (IllegalNetworkDeviceListException Ex) {
				throw new NodeRelatedException(e,
						Messages.NetworkDevLoaderEx_GENERIC_ERROR, Ex);
			}
		}
		return dl;
	}

}
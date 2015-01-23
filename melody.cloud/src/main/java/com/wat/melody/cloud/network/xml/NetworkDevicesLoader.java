package com.wat.melody.cloud.network.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevicesLoader {

	/**
	 * XML Nested element of an Instance Element, which contains the definition
	 * of a Network Device.
	 */
	public static final String DEFAULT_NETWORK_DEVICE_ELEMENT = "interface";

	/**
	 * XML attribute of a Network Device Element, which define its name.
	 */
	public static final String DEVICE_NAME_ATTR = "device-name";

	/**
	 * XML attribute of a Network Device Element, which define its mac address.
	 */
	public static final String MAC_ATTR = "mac";

	/**
	 * XML attribute of a Network Device Element, which define its ip.
	 */
	public static final String IP_ATTR = "ip";

	/**
	 * XML attribute of a Network Device Element, which define its fqdn.
	 */
	public static final String FQDN_ATTR = "fqdn";

	/**
	 * XML attribute of a Network Device Element, which define its nat-ip.
	 */
	public static final String NAT_IP_ATTR = "nat-ip";

	/**
	 * XML attribute of a Network Device Element, which define its nat-fqdn.
	 */
	public static final String NAT_FQDN_ATTR = "nat-fqdn";

	/**
	 * XML attribute of a Network Device Element, which indicate if the timeout
	 * of the network device attachment operation.
	 */
	public static final String TIMEOUT_ATTACH_ATTR = "timeout-attach";

	/**
	 * XML attribute of a Network Device Element, which indicate if the timeout
	 * of the network device detachment operation.
	 */
	public static final String TIMEOUT_DETACH_ATTR = "timeout-detach";

	public NetworkDevicesLoader() {
	}

	private NetworkDeviceName loadDeviceName(Element e)
			throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + DEVICE_NAME_ATTR,
					null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return NetworkDeviceName.parseString(v);
			} catch (IllegalNetworkDeviceNameException Ex) {
				Attr attr = DocHelper.getAttribute(e, "./@" + DEVICE_NAME_ATTR,
						null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private String loadMac(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + MAC_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private String loadIp(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + IP_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private String loadFqdn(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + FQDN_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private String loadNatIp(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper
					.getAttributeValue(e, "./@" + NAT_IP_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private String loadNatFqdn(Element e) throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@" + NAT_FQDN_ATTR,
					null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return v;
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private GenericTimeout loadAttachTimeout(Element e)
			throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@"
					+ TIMEOUT_ATTACH_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return GenericTimeout.parseString(v);
			} catch (IllegalTimeoutException Ex) {
				Attr attr = DocHelper.getAttribute(e, "./@"
						+ TIMEOUT_ATTACH_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	private GenericTimeout loadDetachTimeout(Element e)
			throws NodeRelatedException {
		try {
			String v = DocHelper.getAttributeValue(e, "./@"
					+ TIMEOUT_DETACH_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return GenericTimeout.parseString(v);
			} catch (IllegalTimeoutException Ex) {
				Attr attr = DocHelper.getAttribute(e, "./@"
						+ TIMEOUT_DETACH_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * <p>
	 * Find the Network Device {@link Element}s of the given Instance
	 * {@link Element} and convert it into a {@link NetworkDeviceList}.
	 * </p>
	 * 
	 * <p>
	 * A Network Device {@link Element} may have the attributes : <BR/>
	 * <ul>
	 * <li>device-name : (mandatory) which must contains a
	 * {@link NetworkDeviceName} ;</li>
	 * <li>ip : which must contains a <tt>String</tt> ;</li>
	 * <li>fqdn : which must contains a <tt>String</tt> ;</li>
	 * <li>nat-ip : which must contains a <tt>String</tt> ;</li>
	 * <li>nat-fqdn : which must contains a <tt>String</tt> ;</li>
	 * <li>timeout-attach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>timeout-detach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return a {@link NetworkDeviceList} object, which is a collection of
	 *         {@link NetworkDevice}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the conversion failed (ex : invalid device name,
	 *             multiple declaration with the same device name, invalid
	 *             timeout value) ;</li>
	 *             <li>if no network device can be found ;</li>
	 *             </ul>
	 */
	public NetworkDeviceList load(Element instanceElmt)
			throws NodeRelatedException {
		List<Element> networkDeviceElmts = NetworkDevicesHelper
				.findNetworkDeviceElements(instanceElmt);

		NetworkDeviceList dl = new NetworkDeviceList();
		for (Element networkDeviceElmt : networkDeviceElmts) {
			NetworkDeviceName netDevName = loadDeviceName(networkDeviceElmt);
			if (netDevName == null) {
				throw new NodeRelatedException(networkDeviceElmt, Msg.bind(
						Messages.NetworkDevLoaderEx_MISSING_ATTR,
						DEVICE_NAME_ATTR));
			}
			String mac = loadMac(networkDeviceElmt);
			String ip = loadIp(networkDeviceElmt);
			String fqdn = loadFqdn(networkDeviceElmt);
			String natip = loadNatIp(networkDeviceElmt);
			String natfqdn = loadNatFqdn(networkDeviceElmt);
			GenericTimeout attachTimeout = loadAttachTimeout(networkDeviceElmt);
			GenericTimeout detachTimeout = loadDetachTimeout(networkDeviceElmt);

			try {
				dl.addNetworkDevice(new NetworkDevice(netDevName, mac, ip,
						fqdn, natip, natfqdn, attachTimeout, detachTimeout));
			} catch (IllegalNetworkDeviceListException Ex) {
				throw new NodeRelatedException(networkDeviceElmt,
						Messages.NetworkDevLoaderEx_GENERIC_ERROR, Ex);
			}
		}
		if (dl.size() == 0) {
			throw new NodeRelatedException(instanceElmt, Msg.bind(
					Messages.NetworkDevLoaderEx_EMPTY_NETDEV_LIST,
					NetworkDevicesHelper.NETWORK_DEVICE_ELEMENTS_SELECTOR,
					NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		return dl;
	}

}
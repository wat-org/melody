package com.wat.melody.cloud.network.activation.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationProtocolException;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.telnet.TelnetNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatas;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.cloud.network.xml.NetworkDevicesLoader;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivationHelper {

	/**
	 * XML attribute of the Network Management Element, which contains the XPath
	 * Expression criteria to select Network Activation Device Element.
	 */
	public static final String NETWORK_ACTIVATION_DEVICE_CRITERIA = "activation-device-selector";

	/**
	 * Default XPath Expression to select Network Activation Device Element
	 * (more formally called the "Network Activation Device Selector").
	 */
	public static final String DEFAULT_NETOWRK_ACTIVATION_DEVICE_CRITERIA = "@"
			+ NetworkDevicesLoader.DEVICE_NAME_ATTR + "='eth0'";

	/**
	 * XML attribute of the Network Management Element, which contains the name
	 * of the attribute of the Network Activation Device Element which contains
	 * the Host to activate.
	 */
	public static final String NETWORK_ACTIVATION_HOST_SELECTOR = "activation-host-selector";

	/**
	 * Default name of the attribute of the Network Activation Device Element
	 * which contains the Host to activate (more formally called the
	 * "Network Activation Host Selector").
	 */
	public static final String DEFAULT_NETWORK_ACTIVATION_HOST_SELECTOR = "ip";

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Network Activation Device Selector, which is :
	 *         <ul>
	 *         <li>The Default Network Activation Device Selector, if the given
	 *         element's has no Network Management Element ;</li>
	 *         <li>The Default Network Activation Device Selector, if the given
	 *         element has a Network Management Element which has has no Custom
	 *         Network Activation Device Selector defined in ;</li>
	 *         <li>The Custom Network Activation Device Selector defined in the
	 *         given element's Network Management Element ;</li>
	 *         </ul>
	 */
	public static String getNetworkActivationDeviceSelector(Element e) {
		return NetworkDevicesHelper.getNetworkDeviceElementsSelector(e) + "["
				+ getNetworkActivationDeviceSelectorAttr(e).getValue() + "]";
	}

	private static Attr getNetworkActivationDeviceSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NETWORK_ACTIVATION_DEVICE_CRITERIA,
					DEFAULT_NETOWRK_ACTIVATION_DEVICE_CRITERIA);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the given Instance's Network Activation Device {@link Element}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if Custom Network Devices Selector (found in the given
	 *             Instance's Network Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects no
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects
	 *             multiple {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             </ul>
	 */
	public static Element findNetworkActivationDeviceElement(Element e)
			throws NodeRelatedException {
		NodeList nl = null;
		String selector = getNetworkActivationDeviceSelector(e);
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, e);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					NetworkDevicesHelper.findNetworkManagementElement(e),
					Msg.bind(
							Messages.NetworkActivationEx_INVALID_XPATH,
							selector,
							NetworkDevicesHelper.NETWORK_DEVICE_ELEMENTS_SELECTOR,
							NETWORK_ACTIVATION_DEVICE_CRITERIA,
							NetworkDevicesHelper.NETWORK_MGMT_ELEMENT), Ex);
		}
		if (nl != null && nl.getLength() > 1) {
			throw new NodeRelatedException(
					NetworkDevicesHelper.findNetworkManagementElement(e),
					Msg.bind(
							Messages.NetworkActivationEx_TOO_MANY_MATCH,
							selector,
							nl.getLength(),
							NetworkDevicesHelper.NETWORK_DEVICE_ELEMENTS_SELECTOR,
							NETWORK_ACTIVATION_DEVICE_CRITERIA,
							NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		if (nl == null || nl.getLength() == 0) {
			throw new NodeRelatedException(
					NetworkDevicesHelper.findNetworkManagementElement(e),
					Msg.bind(
							Messages.NetworkActivationEx_NO_MATCH,
							selector,
							NetworkDevicesHelper.NETWORK_DEVICE_ELEMENTS_SELECTOR,
							NETWORK_ACTIVATION_DEVICE_CRITERIA,
							NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(
					NetworkDevicesHelper.findNetworkManagementElement(e),
					Msg.bind(
							Messages.NetworkActivationEx_NOT_MATCH_ELMT,
							selector,
							nl.item(0).getNodeType(),
							NetworkDevicesHelper.NETWORK_DEVICE_ELEMENTS_SELECTOR,
							NETWORK_ACTIVATION_DEVICE_CRITERIA,
							NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		return (Element) nl.item(0);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the name of the given Instance's Network Activation Device.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if Custom Network Devices Selector (found in the given
	 *             Instance's Network Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects no
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects
	 *             multiple {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             <li>if the given Instance's Network Activation Device Element
	 *             doesn't have a {@link NetworkDevicesLoader#DEVICE_NAME_ATTR}
	 *             {@link Attr} ;</li>
	 *             <li>if the value found in the given Instance's Network
	 *             Activation Device Element's
	 *             {@link NetworkDevicesLoader#DEVICE_NAME_ATTR} {@link Attr}
	 *             cannot be converted to a {@link NetworkDeviceName} ;</li>
	 *             </ul>
	 */
	public static NetworkDeviceName findNetworkActivationDeviceName(
			Element instanceElmt) throws NodeRelatedException {
		Element netElmt = findNetworkActivationDeviceElement(instanceElmt);
		String attr = NetworkDevicesLoader.DEVICE_NAME_ATTR;
		try {
			return NetworkDeviceName.parseString(netElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			throw new NodeRelatedException(netElmt, Msg.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new NodeRelatedException(netElmt, Msg.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * @param instanceElmts
	 *            is list of {@link Element}. Each {@link Element} describes an
	 *            Instance.
	 * 
	 * @return a list which contains the Activation Host defined in each
	 *         Instance's Network Activation Device. Can be an empty list, if no
	 *         Instance, if no Activation Host is found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance list is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if Custom Network Devices Selector (found in one of the
	 *             given Instance's Network Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             one of the given Instance's Network Management Element)
	 *             selects no {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             one of the given Instance's Network Management Element)
	 *             selects multiple {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             one of the given Instance's Network Management Element)
	 *             doesn't select an {@link Element} ;</li>
	 *             <li>if the Activation Host (found in one of the given
	 *             Instance's Network Activation Device Element) cannot be
	 *             converted to an {@link Host} ;</li>
	 *             </ul>
	 */
	public static List<Host> findNetworkActivationHost(
			List<Element> instanceElmts) throws NodeRelatedException {
		if (instanceElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<Host> list = new ArrayList<Host>();
		for (Element instanceElmt : instanceElmts) {
			if (instanceElmt == null) {
				continue;
			}
			Host host = findNetworkActivationHost(instanceElmt);
			if (host != null) {
				list.add(host);
			}
		}
		return list;
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Host defined in the given Instance's Network
	 *         Activation Device, or <tt>null</tt> if no Activation Host is
	 *         found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if Custom Network Devices Selector (found in the given
	 *             Instance's Network Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects no
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) selects
	 *             multiple {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             <li>if the Activation Host (found in the given Instance's
	 *             Network Activation Device Element) cannot be converted to an
	 *             {@link Host} ;</li>
	 *             </ul>
	 */
	public static Host findNetworkActivationHost(Element instanceElmt)
			throws NodeRelatedException {
		Element netElmt = findNetworkActivationDeviceElement(instanceElmt);
		String attr = findNetworkActivationHostSelector(instanceElmt);
		try {
			return Host.parseString(netElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return null;
		} catch (IllegalHostException Ex) {
			throw new NodeRelatedException(
					NetworkDevicesHelper
							.findNetworkManagementElement(instanceElmt),
					Msg.bind(Messages.NetMgmtEx_INVALID_ATTR,
							NETWORK_ACTIVATION_HOST_SELECTOR),
					new MelodyException(
							Msg.bind(
									Messages.NetworkActivationEx_INVALID_NETWORK_ACTIVATION_HOST,
									attr), Ex));
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Network Activation Host Selector, which is :
	 *         <ul>
	 *         <li>The Default Network Activation Host Selector, if the given
	 *         element has no Network Management Element ;</li>
	 *         <li>The Default Network Activation Host Selector, if the given
	 *         element has a Network Management Element which has no Custom
	 *         Network Activation Host Selector defined in ;</li>
	 *         <li>The Custom Network Activation Host Selector defined in the
	 *         given element's Network Management Element ;</li>
	 *         </ul>
	 */
	public static String findNetworkActivationHostSelector(Element e) {
		return findNetworkActivationHostSelectorAttr(e).getNodeValue();
	}

	private static Attr findNetworkActivationHostSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NETWORK_ACTIVATION_HOST_SELECTOR,
					DEFAULT_NETWORK_ACTIVATION_HOST_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Port defined in the given Instance's Network
	 *         Activation Element. If Activation Port is undefined, a default
	 *         Activation Port will be used, regarding to the Activation
	 *         Protocol.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the given element has no Network Management Element ;</li>
	 *             <li>if no Activation Port and no Activation Protocol are
	 *             defined in the given element's Network Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol} ;</li>
	 *             <li>if the Activation Port defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link Port} ;</li>
	 *             </ul>
	 */
	public static Port findNetworkActivationPort(Element instanceElmt)
			throws NodeRelatedException {
		Attr attr = findNetworkActivationPortAttr(instanceElmt);
		if (attr == null) {
			// if no Network Activation Port is defined
			// then try to deduce the default port from the Network
			// Activation Protocol
			return getDefaultNetworkActivationPort(instanceElmt);
		}
		try {
			return Port.parseString(attr.getNodeValue());
		} catch (IllegalPortException Ex) {
			throw new NodeRelatedException(
					NetworkDevicesHelper
							.findNetworkManagementElement(instanceElmt),
					Msg.bind(Messages.NetMgmtEx_INVALID_ATTR,
							NetworkActivationDatasLoader.ACTIVATION_PORT_ATTR),
					Ex);
		}
	}

	private static Attr findNetworkActivationPortAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NetworkActivationDatasLoader.ACTIVATION_PORT_ATTR, null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the default Activation Port, regarding to the Activation Protocol
	 *         of the given element's Network Management Element.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given element is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the given element has no Network Management Element ;</li>
	 *             <li>if no Activation Protocol is defined in the given
	 *             element's Network Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given element's
	 *             Network Activation Protocol cannot be converted to a
	 *             {@link NetworkActivationProtocol} ;</li>
	 *             </ul>
	 */
	private static Port getDefaultNetworkActivationPort(Element instanceElmt)
			throws NodeRelatedException {
		NetworkActivationProtocol ap = findNetworkActivationProtocol(instanceElmt);
		if (ap == null) {
			// throw a precise error message
			Element mgmtElmt = NetworkDevicesHelper
					.findNetworkManagementElement(instanceElmt);
			if (mgmtElmt == null) {
				// if no Network Management Element is defined
				throw new NodeRelatedException(instanceElmt, Msg.bind(
						Messages.NetMgmtEx_MISSING,
						NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
			}
			// if no Network Activation Protocol is defined
			throw new NodeRelatedException(mgmtElmt, Msg.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					NetworkActivationDatasLoader.ACTIVATION_PROTOCOL_ATTR));
		}
		switch (ap) {
		case SSH:
			return SshNetworkActivationDatas.DEFAULT_PORT;
		case TELNET:
			return TelnetNetworkActivationDatas.DEFAULT_PORT;
		case WINRM:
			return WinRmNetworkActivationDatas.DEFAULT_PORT;
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "on an unknown Activation Protocol '" + ap + "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Protocol defined in the given element's Network
	 *         Management Element, or <tt>null</tt> if no Activation Protocol is
	 *         found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given element is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Activation Protocol defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol}.
	 */
	public static NetworkActivationProtocol findNetworkActivationProtocol(
			Element instanceElmt) throws NodeRelatedException {
		Attr attr = findNetworkActivationProtocolAttr(instanceElmt);
		if (attr == null) {
			return null;
		}
		try {
			return NetworkActivationProtocol.parseString(attr.getNodeValue());
		} catch (IllegalNetworkActivationProtocolException Ex) {
			throw new NodeRelatedException(
					NetworkDevicesHelper
							.findNetworkManagementElement(instanceElmt),
					Msg.bind(
							Messages.NetMgmtEx_INVALID_ATTR,
							NetworkActivationDatasLoader.ACTIVATION_PROTOCOL_ATTR),
					Ex);
		}
	}

	private static Attr findNetworkActivationProtocolAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NetworkActivationDatasLoader.ACTIVATION_PROTOCOL_ATTR,
					null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Enable defined in the given Instance's Network
	 *         Activation Element. Or <tt>true</tt>, if no Activation Enable is
	 *         defined in the given Instance's Network Management. Or
	 *         <tt>false</tt> if the given Instance has no Network Management
	 *         Element.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 */
	public static boolean isNetworkActivationEnabled(Element instanceElmt) {
		Attr attr = findNetworkActivationEnabledAttr(instanceElmt);
		if (attr == null) {
			Element mgmtElmt = NetworkDevicesHelper
					.findNetworkManagementElement(instanceElmt);
			return mgmtElmt == null ? false : true;
		}
		try {
			return Bool.parseString(attr.getNodeValue());
		} catch (IllegalBooleanException Ex) {
			return false;
		}
	}

	private static Attr findNetworkActivationEnabledAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NetworkActivationDatasLoader.ACTIVATION_ENABLED_ATTR,
					null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Timeout defined in the given Instance's Network
	 *         Activation Element. If Activation Timeout is undefined, a default
	 *         Activation Timeout will be used, regarding to the Activation
	 *         Protocol.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Activation Timeout defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationTimeout};</li>
	 *             <li>if the Activation Protocol defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol} ;</li>
	 *             <ul>
	 */
	public static NetworkActivationTimeout findNetworkActivationTimeout(
			Element instanceElmt) throws NodeRelatedException {
		Attr attr = findNetworkActivationTimeoutAttr(instanceElmt);
		if (attr == null) {
			// if no Network Activation Timeout is defined
			// then try to deduce the default port from the Network
			// Activation Protocol
			return getDefaultNetworkActivationTimeout(instanceElmt);
		}
		try {
			return NetworkActivationTimeout.parseString(attr.getNodeValue());
		} catch (IllegalTimeoutException Ex) {
			throw new NodeRelatedException(
					NetworkDevicesHelper
							.findNetworkManagementElement(instanceElmt),
					Msg.bind(
							Messages.NetMgmtEx_INVALID_ATTR,
							NetworkActivationDatasLoader.ACTIVATION_TIMEOUT_ATTR),
					Ex);
		}
	}

	private static Attr findNetworkActivationTimeoutAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./"
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT + "/@"
					+ NetworkActivationDatasLoader.ACTIVATION_TIMEOUT_ATTR,
					null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the default Activation Timeout, regarding to the Activation
	 *         Protocol of the given Network Management Element.
	 * 
	 * @throws NodeRelatedException
	 *             if the Activation Protocol defined in the given element's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol}.
	 */
	private static NetworkActivationTimeout getDefaultNetworkActivationTimeout(
			Element instanceElmt) throws NodeRelatedException {
		NetworkActivationProtocol ap = findNetworkActivationProtocol(instanceElmt);
		if (ap == null) {
			return NetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		}
		switch (ap) {
		case SSH:
			return SshNetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		case TELNET:
			return TelnetNetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		case WINRM:
			return WinRmNetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "on an unknown management method '" + ap + "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
	}

}
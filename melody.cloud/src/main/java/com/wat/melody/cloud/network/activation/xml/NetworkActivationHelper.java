package com.wat.melody.cloud.network.activation.xml;

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
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatas;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.cloud.network.xml.NetworkDevicesLoader;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;

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
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the Network Activation Device Selector, which is :
	 *         <ul>
	 *         <li>The Default Network Activation Device Selector, if the given
	 *         Network Management Element is <tt>null</tt> ;</li>
	 *         <li>The Default Network Activation Device Selector, if the given
	 *         Network Management Element is not <tt>null</tt> but has no Custom
	 *         Network Activation Device Selector is defined in ;</li>
	 *         <li>The Custom Network Activation Device Selector defined in the
	 *         given Network Management Element ;</li>
	 *         </ul>
	 */
	public static String getNetworkActivationDeviceSelector(Element mgmtElmt) {
		String criteria = null;
		try {
			criteria = mgmtElmt.getAttributeNode(
					NETWORK_ACTIVATION_DEVICE_CRITERIA).getNodeValue();
		} catch (NullPointerException Ex) {
			criteria = DEFAULT_NETOWRK_ACTIVATION_DEVICE_CRITERIA;
		}
		return NetworkDevicesHelper.getNetworkDeviceElementsSelector(mgmtElmt)
				+ "[" + criteria + "]";
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the Network Activation Host Selector, which is :
	 *         <ul>
	 *         <li>The Default Network Activation Host Selector, if the given
	 *         Network Management Element is <tt>null</tt> ;</li>
	 *         <li>The Default Network Activation Host Selector, if the given
	 *         Network Management Element is not <tt>null</tt> but has no Custom
	 *         Network Activation Host Selector is defined in ;</li>
	 *         <li>The Custom Network Activation Host Selector defined in the
	 *         given Network Management Element ;</li>
	 *         </ul>
	 */
	public static String getNetworkActivationHostSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(NETWORK_ACTIVATION_HOST_SELECTOR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETWORK_ACTIVATION_HOST_SELECTOR;
		}
	}

	/**
	 * @param instanceElmt
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
	 *             the given Instance's Network Management Element) multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             </ul>
	 */
	public static Element findNetworkActivationDeviceElement(
			Element instanceElmt) throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		return getNetworkActivationDeviceElement(instanceElmt, mgmtElmt);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to the given Instance. Can be <tt>null</tt>,
	 *            if the given Instance has no Network Management Element.
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
	 *             the given Instance's Network Management Element) multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             </ul>
	 */
	public static Element getNetworkActivationDeviceElement(
			Element instanceElmt, Element mgmtElmt) throws NodeRelatedException {
		NodeList nl = null;
		String selector = getNetworkActivationDeviceSelector(mgmtElmt);
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetworkActivationEx_INVALID_XPATH, selector), Ex);
		}
		if (nl != null && nl.getLength() > 1) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetworkActivationEx_TOO_MANY_MATCH, selector,
					nl.getLength()));
		}
		if (nl == null || nl.getLength() == 0) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetworkActivationEx_NO_MATCH, selector));
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetworkActivationEx_NOT_MATCH_ELMT, selector, nl
							.item(0).getNodeType()));
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
	 *             <li>if Custom Network Activation Device Selector multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector doesn't
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
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		return getNetworkActivationDeviceName(instanceElmt, mgmtElmt);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to the given Instance. Can be <tt>null</tt>,
	 *            if the given Instance has no Network Management Element.
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
	 *             <li>if Custom Network Activation Device Selector multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector doesn't
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
	public static NetworkDeviceName getNetworkActivationDeviceName(
			Element instanceElmt, Element mgmtElmt) throws NodeRelatedException {
		Element netElmt = getNetworkActivationDeviceElement(instanceElmt,
				mgmtElmt);
		String attr = NetworkDevicesLoader.DEVICE_NAME_ATTR;
		try {
			return NetworkDeviceName.parseString(netElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			throw new NodeRelatedException(netElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new NodeRelatedException(netElmt, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
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
	 *             the given Instance's Network Management Element) multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             <li>if the given Instance's Network Activation Device's
	 *             Network Activation Host Selector's attribute cannot be
	 *             converted to an {@link Host} ;</li>
	 *             </ul>
	 */
	public static Host findNetworkActivationHost(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		return getNetworkActivationHost(instanceElmt, mgmtElmt);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to the given Instance. Can be <tt>null</tt>,
	 *            if the related Instance has no Network Management Element.
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
	 *             the given Instance's Network Management Element) multiple
	 *             {@link Element} ;</li>
	 *             <li>if Custom Network Activation Device Selector (found in
	 *             the given Instance's Network Management Element) doesn't
	 *             select an {@link Element} ;</li>
	 *             <li>if the given Instance's Network Activation Device's
	 *             Network Activation Host Selector's attribute cannot be
	 *             converted to an {@link Host} ;</li>
	 *             </ul>
	 */
	public static Host getNetworkActivationHost(Element instanceElmt,
			Element mgmtElmt) throws NodeRelatedException {
		Element netElmt = getNetworkActivationDeviceElement(instanceElmt,
				mgmtElmt);
		String attr = getNetworkActivationHostSelector(mgmtElmt);
		try {
			return Host.parseString(netElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return null;
		} catch (IllegalHostException Ex) {
			throw new NodeRelatedException(netElmt, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
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
	 *             <li>if the given Instance has no Network Management Element ;
	 *             </li>
	 *             <li>if no Activation Port and no Activation Protocol are
	 *             defined in the given Instance's Network Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given
	 *             Instance's Network Management Element cannot be converted to
	 *             a {@link NetworkActivationProtocol} ;</li>
	 *             <li>if the Activation Port defined in the given Instance's
	 *             Network Management Element cannot be converted to a
	 *             {@link Port} ;</li>
	 *             </ul>
	 */
	public static Port findNetworkActivationPort(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		if (mgmtElmt == null) {
			throw new NodeRelatedException(instanceElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING,
					NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		return getNetworkActivationPort(mgmtElmt);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance.
	 * 
	 * @return the Activation Port defined in the given Network Activation
	 *         Element. If Activation Port is undefined, a default Activation
	 *         Port will be used, regarding to the Activation Protocol.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Management Element is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if no Activation Port and no Activation Protocol are
	 *             defined in the given Network Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given Network
	 *             Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol} ;</li>
	 *             <li>if the Activation Port defined in the given Network
	 *             Management Element cannot be converted to a {@link Port} ;</li>
	 *             </ul>
	 */
	public static Port getNetworkActivationPort(Element mgmtElmt)
			throws NodeRelatedException {
		if (mgmtElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Management Element.");
		}
		String attr = NetworkActivationDatasLoader.ACTIVATION_PORT_ATTR;
		try {
			return Port.parseString(mgmtElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return getDefaultNetworkActivationPort(mgmtElmt);
		} catch (IllegalPortException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance.
	 * 
	 * @return the default Activation Port, regarding to the Activation Protocol
	 *         of the given Network Management Element.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Management Element is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if no Activation Protocol is defined in the given Network
	 *             Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given
	 *             Instance's Network Activation Protocol cannot be converted to
	 *             a {@link NetworkActivationProtocol} ;</li>
	 *             </ul>
	 */
	private static Port getDefaultNetworkActivationPort(Element mgmtElmt)
			throws NodeRelatedException {
		if (mgmtElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Management Element.");
		}
		NetworkActivationProtocol ap = null;
		try {
			ap = getNetworkActivationProtocol(mgmtElmt);
			switch (ap) {
			case SSH:
				return SshNetworkActivationDatas.DEFAULT_PORT;
			case WINRM:
				return WinRmNetworkActivationDatas.DEFAULT_PORT;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown Activation Protocol '" + ap + "'. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.");
			}
		} catch (NodeRelatedException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					NetworkActivationDatasLoader.ACTIVATION_PORT_ATTR), Ex);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Activation Protocol defined in the given Instance's Network
	 *         Management Element.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the given Instance has no Network Management Element ;
	 *             </li>
	 *             <li>if no Activation Protocol is defined in the given
	 *             Instance's Network Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given
	 *             Instance's Network Management Element cannot be converted to
	 *             a {@link NetworkActivationProtocol} ;</li>
	 *             </ul>
	 */
	public static NetworkActivationProtocol findNetworkActivationProtocol(
			Element instanceElmt) throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		if (mgmtElmt == null) {
			throw new NodeRelatedException(instanceElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING,
					NetworkDevicesHelper.NETWORK_MGMT_ELEMENT));
		}
		return getNetworkActivationProtocol(mgmtElmt);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance.
	 * 
	 * @return the Activation Protocol defined in the given Network Management
	 *         Element.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Management Element is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if no Activation Protocol is defined in the given Network
	 *             Management Element ;</li>
	 *             <li>if the Activation Protocol defined in the given Network
	 *             Management Element cannot be converted to a
	 *             {@link NetworkActivationProtocol} ;</li>
	 *             </ul>
	 */
	public static NetworkActivationProtocol getNetworkActivationProtocol(
			Element mgmtElmt) throws NodeRelatedException {
		if (mgmtElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Management Element.");
		}
		String attr = NetworkActivationDatasLoader.ACTIVATION_PROTOCOL_ATTR;
		try {
			return NetworkActivationProtocol.parseString(mgmtElmt
					.getAttributeNode(attr).getNodeValue());
		} catch (NullPointerException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr));
		} catch (IllegalNetworkActivationProtocolException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
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
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		return getNetworkActivationEnabled(mgmtElmt);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the Activation Enable defined in the given Network Activation
	 *         Element. Or <tt>true</tt>, if no Activation Enable is defined in
	 *         the given Network Management. Or <tt>false</tt> if the given
	 *         Network Management Element is <tt>null</tt>.
	 */
	public static boolean getNetworkActivationEnabled(Element mgmtElmt) {
		if (mgmtElmt == null) {
			return false;
		}
		String attr = NetworkActivationDatasLoader.ACTIVATION_ENABLED_ATTR;
		try {
			return Bool.parseString(mgmtElmt.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return true;
		} catch (IllegalBooleanException Ex) {
			return false;
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
	 *             <li>if the Activation Timeout defined in the given Instance's
	 *             Network Management Element cannot be converted to a
	 *             {@link NetworkActivationTimeout} ;</li>
	 *             </ul>
	 */
	public static NetworkActivationTimeout findNetworkActivationTimeout(
			Element instanceElmt) throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		return getNetworkActivationTimeout(mgmtElmt);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the Activation Timeout defined in the given Network Activation
	 *         Element. If Activation Timeout is undefined, a default Activation
	 *         Timeout will be used, regarding to the Activation Protocol.
	 * 
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Activation Timeout defined in the given Network
	 *             Management Element cannot be converted to a
	 *             {@link NetworkActivationTimeout} ;</li>
	 *             </ul>
	 */
	public static NetworkActivationTimeout getNetworkActivationTimeout(
			Element mgmtElmt) throws NodeRelatedException {
		String attr = NetworkActivationDatasLoader.ACTIVATION_TIMEOUT_ATTR;
		try {
			return NetworkActivationTimeout.parseString(mgmtElmt
					.getAttributeNode(attr).getNodeValue());
		} catch (NullPointerException Ex) {
			return getDefaultNetworkActivationTimeout(mgmtElmt);
		} catch (IllegalTimeoutException Ex) {
			throw new NodeRelatedException(mgmtElmt, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the default Activation Timeout, regarding to the Activation
	 *         Protocol of the given Network Management Element.
	 */
	private static NetworkActivationTimeout getDefaultNetworkActivationTimeout(
			Element mgmtElmt) {
		if (mgmtElmt == null) {
			return NetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		}
		NetworkActivationProtocol ap = null;
		try {
			ap = getNetworkActivationProtocol(mgmtElmt);
			switch (ap) {
			case SSH:
				return SshNetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
			case WINRM:
				return WinRmNetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown management method '" + ap + "'. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.");
			}
		} catch (NodeRelatedException Ex) {
			return NetworkActivationDatas.DEFAULT_ACTIVATION_TIMEOUT;
		}
	}

}
package com.wat.melody.cloud.network;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.exception.IllegalManagementMethodNetworkException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkManagementHelper {

	/**
	 * XML Element in the RD, which contains Network Device Management datas of
	 * the related Instance Node (more formally called the
	 * "Network Device Management Node")
	 */
	public static final String NETWORK_MGMT_NE = "network-management";

	/**
	 * XPath Expression which select the Network Device Management Node of the
	 * related Instance Node.
	 */
	public static final String NETWORK_MGMT_NODE_SELECTOR = "//"
			+ NETWORK_MGMT_NE;

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * XPath Expression to select Network Devices.
	 */
	public static final String NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "network-devices-selector";

	/**
	 * Default XPath Expression to select Network Devices.
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR = "//"
			+ NetworkDeviceNamesLoader.INTERFACE_NE;

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * criteria of XPath Expression to select Network Device Management Node.
	 */
	public static final String NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR = "mgmt-network-device-criteria";

	/**
	 * Default XPath Expression to select Network Device Management Node
	 */
	public static final String DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA = "@"
			+ NetworkDeviceNamesLoader.DEVICE_NAME_ATTR + "='eth0'";

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * XML attribute of the Network Device Management Node which select the Host
	 * to manage.
	 */
	public static final String NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR = "mgmt-network-device-attribute";

	/**
	 * Default XML attribute of the Network Device Management Node which select
	 * the Host to manage.
	 */
	public static final String DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR = "ip";

	/**
	 * <p>
	 * Return the {@link List} of Network Device Management {@link Node} related
	 * to the given {@link List} of Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Network Device Management {@link Node} related to the
	 *         given Instance {@link Node}, if one Network Device Management
	 *         {@link Node} is found ;</li>
	 *         <li>The last Network Device Management {@link Node} related to
	 *         the given Instance {@link Node}, if multiple Network Device
	 *         Management {@link Node} were found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link List} of Instance {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if any Instance {@link Node} has no Network Device Management
	 *             {@link Node}.
	 */
	public static List<Element> findNetworkManagementNode(
			List<Element> instanceNodes) throws NodeRelatedException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Element> hl = new ArrayList<Element>();
		for (Element instanceNode : instanceNodes) {
			hl.add(findNetworkManagementNode(instanceNode));
		}
		return hl;
	}

	/**
	 * <p>
	 * Return the Network Device Management {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Network Device Management {@link Node} related to the
	 *         given Instance {@link Node}, if one Network Device Management
	 *         {@link Node} is found ;</li>
	 *         <li>The last Network Device Management {@link Node} related to
	 *         the given Instance {@link Node}, if multiple Network Device
	 *         Management {@link Node} were found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Network Device Management {@link Node} can be found.
	 */
	public static Element findNetworkManagementNode(Element instanceNode)
			throws NodeRelatedException {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceNode,
					NETWORK_MGMT_NODE_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '" + NETWORK_MGMT_NODE_SELECTOR
					+ "'. " + "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() == 0) {
			throw new NodeRelatedException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_NO_MGMT_NODE, NETWORK_MGMT_NE));
		}
		Node mgmtNode = null;
		if (nl.getLength() > 1) {
			mgmtNode = nl.item(nl.getLength() - 1);
		} else {
			mgmtNode = nl.item(0);
		}
		if (mgmtNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(
					instanceNode,
					Messages.bind(
							Messages.NetMgmtEx_MGMT_NETWORK_NODE_SELECTOR_NOT_MATCH_NODE,
							NETWORK_MGMT_NODE_SELECTOR));
		}
		return (Element) mgmtNode;
	}

	/**
	 * <p>
	 * Return the Management Network Device Selector of the given Network Device
	 * Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}, or <tt>null</tt>.
	 * 
	 * @return the concatenation of :
	 *         <ul>
	 *         <li>The Network Devices Selector of the given Network Device
	 *         Management {@link Node} (see
	 *         {@link #getNetworkDevicesSelector(Node)} ;</li>
	 *         <li>The character '[' ;</li>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR} XML Attribute of
	 *         the given Network Device Management {@link Node} or
	 *         {@link #DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA} ;</li>
	 *         <li>The character ']' ;</li>
	 *         </ul>
	 */
	public static String getManagementNetworkDeviceSelector(Element mgmtNode) {
		String sCriteria = null;
		try {
			sCriteria = mgmtNode.getAttributeNode(
					NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			sCriteria = DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA;
		}
		return getNetworkDevicesSelector(mgmtNode) + "[" + sCriteria + "]";
	}

	/**
	 * <p>
	 * Return the Management Network Device Attribute Selector of the given
	 * Network Device Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}, or <tt>null</tt>.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute of the given Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if the
	 *         given Network Device Management {@link Node} was <tt>null</tt> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute can be found ;</li>
	 *         </ul>
	 */
	public static String getManagementNetworkDeviceAttributeSelector(
			Element mgmtNode) {
		try {
			return mgmtNode.getAttributeNode(
					NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the {@link List} of Management Network Device {@link Node} related
	 * to the given {@link List} of Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return the {@link List} of Management Network Device {@link Node}
	 *         related to the given {@link List} of Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link List} of Instance {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if any instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if any instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if any instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if any instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 */
	public static List<Element> findManagementNetworkDeviceNode(
			List<Element> instanceNodes) throws NodeRelatedException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Element> hl = new ArrayList<Element>();
		Element mgmtNode = null;
		for (Element instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (NodeRelatedException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			hl.add(getManagementNetworkDeviceNode(instanceNode, mgmtNode));
		}
		return hl;
	}

	/**
	 * <p>
	 * Return the Management Network Device {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 */
	public static Element findManagementNetworkDeviceNode(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkDeviceNode(instanceNode, mgmtNode);
	}

	/**
	 * <p>
	 * Return the Management Network Device {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 */
	public static Element getManagementNetworkDeviceNode(Element instanceNode,
			Element mgmtNode) throws NodeRelatedException {
		NodeList nl = null;
		String sMgmtInterfaceSelector = getManagementNetworkDeviceSelector(mgmtNode);
		try {
			nl = XPathExpander.evaluateAsNodeList("." + sMgmtInterfaceSelector,
					instanceNode);
			/*
			 * TODO : when we can't find the mgnt net dev, we select the first
			 * one ? crazy
			 * 
			 * else if (nl == null || nl.getLength() == 0) {
			 * sMgmtInterfaceSelector = getNetworkDevicesSelector(mgmtNode); nl
			 * = XPathExpander.evaluateAsNodeList("." + sMgmtInterfaceSelector,
			 * instanceNode); }
			 */
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_SELECTOR,
					sMgmtInterfaceSelector), Ex);
		}
		if (nl != null && nl.getLength() > 1) {
			throw new NodeRelatedException(instanceNode,
					Messages.NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE);
		}
		if (nl == null || nl.getLength() == 0) {
			throw new NodeRelatedException(instanceNode,
					Messages.NetMgmtEx_NO_MGMT_NETWORK_DEVICE);
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(
					instanceNode,
					Messages.bind(
							Messages.NetMgmtEx_MGMT_NETWORK_DEVICE_SELECTOR_NOT_MATCH_NODE,
							sMgmtInterfaceSelector));
		}
		return (Element) nl.item(0);
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return a {@link list} of Management Network Device {@link Host} related
	 *         to the given {@link List} of Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws NodeRelatedException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a attribute equal to the Network Device
	 *             Management Device Attribute Selector.
	 * @throws NodeRelatedException
	 *             if the value of one attribute is not a valid {@link Host}.
	 */
	public static List<Host> findManagementNetworkHost(
			List<Element> instanceNodes) throws NodeRelatedException {
		List<Host> hl = new ArrayList<Host>();
		Element mgmtNode = null;
		for (Element instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (NodeRelatedException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			hl.add(getManagementNetworkHost(instanceNode, mgmtNode));
		}
		return hl;
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device's {@link Host}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws NodeRelatedException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host findManagementNetworkHost(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkHost(instanceNode, mgmtNode);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device {@link Host}.
	 * 
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws NodeRelatedException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host getManagementNetworkHost(Element instanceNode,
			Element mgmtNode) throws NodeRelatedException {
		try {
			return Host.parseString(getManagementNetworkHostNode(instanceNode,
					mgmtNode).getNodeValue());
		} catch (NullPointerException Ex) {
			Element netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
			throw new NodeRelatedException(netNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalHostException Ex) {
			Element netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
			throw new NodeRelatedException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return a {@link list} of Management Network Device {@link Host}
	 *         {@link Node} related to the given {@link List} of Instance
	 *         {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link List} of Instance {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 */
	public static List<Attr> findManagementNetworkHostNode(
			List<Element> instanceNodes) throws NodeRelatedException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Attr> hl = new ArrayList<Attr>();
		Element mgmtNode = null;
		for (Element instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (NodeRelatedException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			Attr n = getManagementNetworkHostNode(instanceNode, mgmtNode);
			if (n != null) {
				hl.add(n);
			}
		}
		return hl;
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device's {@link Host}
	 *         {@link Node}, or <tt>null</tt> if the given Instance doesn't have
	 *         one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Attr findManagementNetworkHostNode(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkHostNode(instanceNode, mgmtNode);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device {@link Host}
	 *         {@link Node}, or <tt>null</tt> if the given Instance doesn't have
	 *         one.
	 * 
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 */
	public static Attr getManagementNetworkHostNode(Element instanceNode,
			Element mgmtNode) throws NodeRelatedException {
		Element netNode = getManagementNetworkDeviceNode(instanceNode, mgmtNode);
		String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
		return netNode.getAttributeNode(attr);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management {@link Port}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#PORT_ATTR} attribute
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link Port}.
	 * @throws NodeRelatedException
	 *             if the {@link ManagementNetworkDatasLoader#PORT_ATTR}
	 *             attribute is not defined in the given Network Device
	 *             Management {@link Node} and the given Network Device
	 *             Management {@link Node} doesn't define a
	 *             ManagementNetworkMethod (which is normaly used to define a
	 *             default port).
	 */
	public static Port findManagementNetworkPort(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkPort(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link Port}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#PORT_ATTR} attribute
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link Port}.
	 * @throws NodeRelatedException
	 *             if the {@link ManagementNetworkDatasLoader#PORT_ATTR}
	 *             attribute is not defined in the given Network Device
	 *             Management {@link Node} and the given Network Device
	 *             Management {@link Node} doesn't define a
	 *             ManagementNetworkMethod (which is normaly used to define a
	 *             default port).
	 */
	public static Port getManagementNetworkPort(Element mgmtNode)
			throws NodeRelatedException {
		try {
			return Port.parseString(getManagementNetworkPortNode(mgmtNode)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return getDefaultMamangementPort(mgmtNode);
		} catch (IllegalPortException Ex) {
			throw new NodeRelatedException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR,
					ManagementNetworkDatasLoader.PORT_ATTR), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management {@link Port} attribute
	 *         as a {@link Node}, or <tt>null</tt> if it doesn't have one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Attr findManagementNetworkPortNode(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkPortNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link Port} attribute as a
	 *         {@link Node}, or <tt>null</tt> if such attribute cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 */
	public static Attr getManagementNetworkPortNode(Element mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		return mgmtNode
				.getAttributeNode(ManagementNetworkDatasLoader.PORT_ATTR);
	}

	/**
	 * <p>
	 * Return the default management port, regarding the
	 * {@link ManagementNetworkMethod}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the default management Port, regarding the
	 *         {@link ManagementNetworkMethod}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the given Network Device Management {@link Node} doesn't
	 *             define a ManagementNetworkMethod (which is normaly used to
	 *             define a default port).
	 */
	private static Port getDefaultMamangementPort(Element mgmtNode)
			throws NodeRelatedException {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		ManagementNetworkMethod mm = null;
		try {
			mm = getManagementNetworkMethod(mgmtNode);
			switch (mm) {
			case SSH:
				return SshManagementNetworkDatas.DEFAULT_PORT;
			case WINRM:
				return WinRmManagementNetworkDatas.DEFAULT_PORT;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown management method '" + mm + "'. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.");
			}
		} catch (NodeRelatedException Ex) {
			throw new NodeRelatedException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					ManagementNetworkDatasLoader.PORT_ATTR), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management
	 *         {@link ManagementNetworkMethod}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws NodeRelatedException
	 *             if no {@link ManagementNetworkDatasLoader#METHOD_ATTR} can be
	 *             found in the given Instance's Network Device Management
	 *             {@link Node}.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#METHOD_ATTR} found in the
	 *             given Instance's Network Device Management {@link Node} is
	 *             not a valid {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod findManagementNetworkMethod(
			Element instanceNode) throws NodeRelatedException {
		Element mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkMethod(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link ManagementNetworkMethod}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no {@link ManagementNetworkDatasLoader#METHOD_ATTR} can be
	 *             found in the given Network Device Management {@link Node}.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#METHOD_ATTR} found in the
	 *             given Network Device Management {@link Node} is not a valid
	 *             {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod getManagementNetworkMethod(
			Element mgmtNode) throws NodeRelatedException {
		try {
			return ManagementNetworkMethod
					.parseString(getManagementNetworkMethodNode(mgmtNode)
							.getNodeValue());
		} catch (NullPointerException Ex) {
			throw new NodeRelatedException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					ManagementNetworkDatasLoader.METHOD_ATTR));
		} catch (IllegalManagementMethodNetworkException Ex) {
			throw new NodeRelatedException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR,
					ManagementNetworkDatasLoader.METHOD_ATTR), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management
	 *         {@link ManagementNetworkMethod} attribute as a {@link Node}, or
	 *         <tt>null</tt> it doesn't have one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Attr findManagementNetworkMethodNode(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkMethodNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link ManagementNetworkMethod}
	 *         Attribute as a {@link Node}, or <tt>null</tt> if such attribute
	 *         cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 */
	public static Attr getManagementNetworkMethodNode(Element mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		return mgmtNode
				.getAttributeNode(ManagementNetworkDatasLoader.METHOD_ATTR);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Instance's Network Device Management {@link Node}'s
	 *         {@link ManagementNetworkDatasLoader#ENABLE_ATTR} XML Attribute ;</li>
	 *         <li><code>false</code> if the given Instance has no Network
	 *         Management {@link Node} ;</li>
	 *         <li><code>true</code> if the given Instance's Network Device
	 *         Management {@link Node} has no
	 *         {@link ManagementNetworkDatasLoader#ENABLE_ATTR} XML Attribute ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 */
	public static boolean isManagementNetworkEnable(Element instanceNode) {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			return false;
		}
		return getManagementNetworkEnable(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Network Device Management {@link Node}'s
	 *         {@link ManagementNetworkDatasLoader#ENABLE_ATTR} XML Attribute ;</li>
	 *         <li><code>false</code> if the given Network Device Management
	 *         {@link Node} is <tt>null</tt> ;</li>
	 *         <li><code>true</code> if the given Network Device Management
	 *         {@link Node} has no
	 *         {@link ManagementNetworkDatasLoader#ENABLE_ATTR} XML Attribute ;</li>
	 *         </ul>
	 */
	public static boolean getManagementNetworkEnable(Element mgmtNode) {
		if (mgmtNode == null) {
			return false;
		}
		String attr = ManagementNetworkDatasLoader.ENABLE_ATTR;
		try {
			return Boolean.parseBoolean(mgmtNode.getAttributeNode(attr)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return true;
		}
	}

	/**
	 * <p>
	 * Return the Network Devices Selector of the given Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute of
	 *         the given Instance's Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance has no Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance's Network Device Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 */
	public static String findNetworkDevicesSelector(Element instanceNode) {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getNetworkDevicesSelector(mgmtNode);
	}

	/**
	 * <p>
	 * Return the Network Devices Selector of the given Network Device
	 * Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute of
	 *         the given Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Network Device Management {@link Node} is <tt>null</tt> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Network Device Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 */
	public static String getNetworkDevicesSelector(Element mgmtNode) {
		try {
			return mgmtNode.getAttributeNode(
					NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node} of each Instance {@link Node} of
	 * the given list, whose Device Name match the given name.
	 * </p>
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of each given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link List} of Instance {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if any Network Devices Selector (found in the Network Device
	 *             Management {@link Node} of the instance) is not a valid XPath
	 *             Expression.
	 */
	public static List<Element> findNetworkDeviceNodeByName(
			List<Element> instanceNodes, String netDevName)
			throws NodeRelatedException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Element> hl = new ArrayList<Element>();
		for (Element instanceNode : instanceNodes) {
			NodeList nl = findNetworkDeviceNodeByName(instanceNode, netDevName);
			for (int i = 0; i < nl.getLength(); i++) {
				hl.add((Element) nl.item(i));
			}
		}
		return hl;
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node} of the given Instance {@link Node}
	 * whose Device Name match the given network device name.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of the given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given network device name, or all Network Device {@link Node}
	 *         s of the given Instance {@link Node} if the given network device
	 *         name is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node} of the given instance) is not a valid
	 *             XPath Expression.
	 */
	public static NodeList findNetworkDeviceNodeByName(Element instanceNode,
			String netDevName) throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getNetworkDeviceNodeByName(instanceNode, mgmtNode, netDevName);
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node} of the given Instance {@link Node}
	 * whose Device Name match the given name.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of the given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given name.
	 * 
	 * @throws NodeRelatedException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList getNetworkDeviceNodeByName(Element instanceNode,
			Element mgmtNode, String netDevName) throws NodeRelatedException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance Node.");
		}
		String sAllNetDevSelector = getNetworkDevicesSelector(mgmtNode);
		String sNetDevCriteria = "";
		if (netDevName != null) {
			sNetDevCriteria = "[@" + NetworkDeviceNamesLoader.DEVICE_NAME_ATTR
					+ "='" + netDevName + "']";
		}
		String sNetDevSelector = "." + sAllNetDevSelector + sNetDevCriteria;
		try {
			return XPathExpander.evaluateAsNodeList(sNetDevSelector,
					instanceNode);
		} catch (XPathExpressionException Ex) {
			Attr attr = mgmtNode
					.getAttributeNode(NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new NodeRelatedException(attr, Messages.bind(
					Messages.NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR,
					sAllNetDevSelector), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return a {@link list} of Management Network Device
	 *         {@link NetworkDeviceName} related to the given {@link List} of
	 *         Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws NodeRelatedException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a 'device' attribute.
	 * @throws NodeRelatedException
	 *             if the value of one attribute is not a valid
	 *             {@link NetworkDeviceName}.
	 */
	public static List<NetworkDeviceName> findManagementNetworkDeviceName(
			List<Element> instanceNodes) throws NodeRelatedException {
		List<NetworkDeviceName> ndl = new ArrayList<NetworkDeviceName>();
		Element mgmtNode = null;
		for (Element instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (NodeRelatedException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			ndl.add(getManagementNetworkDeviceName(instanceNode, mgmtNode));
		}
		return ndl;
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device's
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a 'device' attribute.
	 * @throws NodeRelatedException
	 *             if the found value is not a valid {@link NetworkDeviceName}.
	 */
	public static NetworkDeviceName findManagementNetworkDeviceName(
			Element instanceNode) throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkDeviceName(instanceNode, mgmtNode);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device
	 *             {@link NetworkDeviceName} doesn't have a 'device' attribute.
	 * @throws NodeRelatedException
	 *             if the found value is not a valid {@link NetworkDeviceName}.
	 */
	public static NetworkDeviceName getManagementNetworkDeviceName(
			Element instanceNode, Element mgmtNode) throws NodeRelatedException {
		try {
			return NetworkDeviceName
					.parseString(getManagementNetworkDeviceNameNode(
							instanceNode, mgmtNode).getNodeValue());
		} catch (NullPointerException Ex) {
			Element netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = NetworkDeviceNamesLoader.DEVICE_NAME_ATTR;
			throw new NodeRelatedException(netNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalNetworkDeviceNameException Ex) {
			Element netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = NetworkDeviceNamesLoader.DEVICE_NAME_ATTR;
			throw new NodeRelatedException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return a {@link list} of Management Network Device
	 *         {@link NetworkDeviceName} {@link Node} related to the given
	 *         {@link List} of Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link List} of Instance {@link Node} is
	 *             <tt>null</tt> or contains <tt>null</tt> elements.
	 * @throws NodeRelatedException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 */
	public static List<Attr> findManagementNetworkDeviceNameNode(
			List<Element> instanceNodes) throws NodeRelatedException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Attr> hl = new ArrayList<Attr>();
		Element mgmtNode = null;
		for (Element instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (NodeRelatedException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			Attr n = getManagementNetworkDeviceNameNode(instanceNode, mgmtNode);
			if (n != null) {
				hl.add(n);
			}
		}
		return hl;
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device's
	 *         {@link NetworkDeviceName} {@link Node}, or <tt>null</tt> if it
	 *         doens't have one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Attr findManagementNetworkDeviceNameNode(Element instanceNode)
			throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkDeviceNameNode(instanceNode, mgmtNode);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <tt>null</tt>, if the given
	 *            Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device
	 *         {@link NetworkDeviceName} {@link Node}, or <tt>null</tt> if it
	 *         doens't have one.
	 * 
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             no {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector selects
	 *             too many {@link Element}.
	 * @throws NodeRelatedException
	 *             if the instance's Management Network Device Selector doesn't
	 *             select an {@link Element}.
	 */
	public static Attr getManagementNetworkDeviceNameNode(Element instanceNode,
			Element mgmtNode) throws NodeRelatedException {
		Element netNode = getManagementNetworkDeviceNode(instanceNode, mgmtNode);
		String attr = NetworkDeviceNamesLoader.DEVICE_NAME_ATTR;
		return netNode.getAttributeNode(attr);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management
	 *         {@link ManagementNetworkEnableTimeout}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#ENABLE_TIMEOUT_ATTR}
	 *             found in the given Instance's Network Device Management
	 *             {@link Node} is not a valid
	 *             {@link ManagementNetworkEnableTimeout}.
	 */
	public static ManagementNetworkEnableTimeout findManagementNetworkEnableTimeout(
			Element instanceNode) throws NodeRelatedException {
		Element mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (NodeRelatedException Ex) {
			return getDefaultManagementEnableTimeout(mgmtNode);
		}
		return getManagementNetworkEnableTimeout(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management
	 *         {@link ManagementNetworkEnableTimeout}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#ENABLE_TIMEOUT_ATTR}
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link ManagementNetworkEnableTimeout}.
	 */
	public static ManagementNetworkEnableTimeout getManagementNetworkEnableTimeout(
			Element mgmtNode) throws NodeRelatedException {
		Attr n = getManagementNetworkEnableTimeoutNode(mgmtNode);
		if (n == null) {
			return getDefaultManagementEnableTimeout(mgmtNode);
		}
		try {
			return ManagementNetworkEnableTimeout.parseString(n.getNodeValue());
		} catch (IllegalTimeoutException Ex) {
			String attr = ManagementNetworkDatasLoader.ENABLE_TIMEOUT_ATTR;
			throw new NodeRelatedException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management enable-timeout attribute
	 *         as a {@link Node}, or <tt>null</tt> if no enable-timeout
	 *         attribute can be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Attr findManagementNetworkEnableTimeoutNode(
			Element instanceNode) throws NodeRelatedException {
		Element mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkEnableTimeoutNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management enable-timeout attribute as a
	 *         {@link Node}, or <tt>null</tt> if no enable-timeout attribute can
	 *         be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <tt>null</tt>.
	 */
	public static Attr getManagementNetworkEnableTimeoutNode(Element mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		String attr = ManagementNetworkDatasLoader.ENABLE_TIMEOUT_ATTR;
		return mgmtNode.getAttributeNode(attr);
	}

	/**
	 * <p>
	 * Return the default {@link ManagementNetworkEnableTimeout} of the given
	 * Instance's Network Device Management {@link Node}, regarding the given
	 * Instance's {@link ManagementNetworkMethod}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the default {@link ManagementNetworkEnableTimeout} of the given
	 *         Instance's Network Device Management {@link Node}, regarding the
	 *         given Instance's {@link ManagementNetworkMethod}.
	 */
	private static ManagementNetworkEnableTimeout getDefaultManagementEnableTimeout(
			Element mgmtNode) {
		if (mgmtNode == null) {
			return ManagementNetworkDatas.DEFAULT_ENABLE_TIMEOUT;
		}
		ManagementNetworkMethod mm = null;
		try {
			mm = getManagementNetworkMethod(mgmtNode);
			switch (mm) {
			case SSH:
				return SshManagementNetworkDatas.DEFAULT_ENABLE_TIMEOUT;
			case WINRM:
				return WinRmManagementNetworkDatas.DEFAULT_ENABLE_TIMEOUT;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown management method '" + mm + "'. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.");
			}
		} catch (NodeRelatedException Ex) {
			return ManagementNetworkDatas.DEFAULT_ENABLE_TIMEOUT;
		}
	}

}
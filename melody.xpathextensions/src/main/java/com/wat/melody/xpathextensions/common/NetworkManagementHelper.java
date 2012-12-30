package com.wat.melody.xpathextensions.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.IllegalManagementMethodException;

public abstract class NetworkManagementHelper {

	private static Log log = LogFactory.getLog(NetworkManagementHelper.class);

	/**
	 * The 'enableNetworkManagement' XML attribute to use in the sequence
	 * descriptor
	 */
	public static final String ENABLE_NETWORK_MGNT_ATTR = "enableNetworkManagement";

	/**
	 * The 'enableNetworkManagementTimeout' XML attribute to use in the sequence
	 * descriptor
	 */
	public static final String ENABLE_NETWORK_MGNT_TIMEOUT_ATTR = "enableNetworkManagement";

	/**
	 * The XML Element which contains Network Management datas in the RD (e.g
	 * the Network Management Node)
	 */
	public static final String NETWORK_MGMT_NODE = "network-management";

	/**
	 * XPath Expression to select Network Management Node in the RD, related to
	 * an Instance Node
	 */
	public static final String NETWORK_MGMT_NODE_SELECTOR = "//"
			+ NETWORK_MGMT_NODE;

	/**
	 * The 'enable' XML attribute of the Network Management Node
	 */
	public static final String NETWORK_MGMT_ENABLE_ATTRIBUTE = "enable";

	/**
	 * The 'method' XML attribute of the Network Management Node
	 */
	public static final String NETWORK_MGMT_METHOD_ATTRIBUTE = "method";

	/**
	 * The 'port' XML attribute of the Network Management Node
	 */
	public static final String NETWORK_MGMT_PORT_ATTRIBUTE = "port";

	/**
	 * The XML attribute of the Network Management Node, which contains the
	 * XPath Expression to select Management Network Device Node
	 */
	public static final String NETWORK_MGMT_DEVICE_NODE_SELECTOR_ATTRIBUTE = "networkMgmtDeviceSelector";

	/**
	 * The XML attribute of the Network Management Node, which contains the XML
	 * attribute of the Management Network Device Node which select the Host to
	 * manage
	 */
	public static final String NETWORK_MGMT_DEVICE_ATTR_SELECTOR_ATTRIBUTE = "networkMgmtDeviceAttribute";

	/**
	 * Default XPath Expression to select Management Network Device Node
	 */
	public static final String DEFAULT_NETOWRK_MGMT_DEVICE_NODE_SELECTOR = "//interface[@device='eth0']";

	/**
	 * Default XML attribute of the Management Network Device Node which select
	 * the Host to manage
	 */
	public static final String DEFAULT_NETWORK_MGMT_DEVICE_ATTR_SELECTOR = "ip";

	/**
	 * The XML attribute which contains the XPath Expression to select Network
	 * Device Nodes
	 */
	public static final String NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "networkDevicesSelector";

	/**
	 * Default XPath Expression to select Network Device Nodes
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR = "//interface";

	/**
	 * <p>
	 * Return the Network Management {@link Node} related to the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return <ul>
	 *         <li>The Network Management {@link Node} related to the given
	 *         Instance {@link Node}, if one Network Management {@link Node} is
	 *         found ;</li>
	 *         <li>The last Network Management {@link Node} related to the given
	 *         Instance {@link Node}, if multiple Network Management
	 *         {@link Node} is found ;</li>
	 *         </ul>
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Network Management {@link Node} can be found.
	 */
	public static Node findNetworkManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
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
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_NO_MGMT_NODE, NETWORK_MGMT_NODE));
		} else if (nl.getLength() > 1) {
			log.debug(Messages.bind(Messages.NetMgmtMsg_TOO_MANY_MGMT_NODE,
					NETWORK_MGMT_NODE, Doc.getNodeLocation(instanceNode)
							.toFullString()));
			return nl.item(nl.getLength() - 1);
		}
		return nl.item(0);
	}

	/**
	 * <p>
	 * Return the Network Management Interface Node Selector of the given
	 * Network Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}, or <code>null</code>.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_NODE_SELECTOR_ATTRIBUTE} XML
	 *         Attribute of the given Network Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_MGMT_DEVICE_NODE_SELECTOR} if the given
	 *         Network Management {@link Node} was <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_MGMT_DEVICE_NODE_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_DEVICE_NODE_SELECTOR_ATTRIBUTE} XML
	 *         Attribute can be found ;</li>
	 *         </ul>
	 */
	public static String findManagementNetworkDeviceSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_NODE_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_MGMT_DEVICE_NODE_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Management Network Device Attribute Selector of the given
	 * Network Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}, or <code>null</code>.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_ATTR_SELECTOR_ATTRIBUTE} XML
	 *         Attribute of the given Network Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTR_SELECTOR} if the given
	 *         Network Management {@link Node} was <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTR_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_DEVICE_ATTR_SELECTOR_ATTRIBUTE} XML
	 *         Attribute can be found ;</li>
	 *         </ul>
	 */
	public static String findManagementNetworkDeviceAttribute(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_ATTR_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETWORK_MGMT_DEVICE_ATTR_SELECTOR;
		}
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
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if the Network Management Device Node Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node findNetworkManagementDevice(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network management datas are invalid.
			// in this situation, we will use default values
		}
		return getNetworkManagementDevice(mgmtNode, instanceNode);
	}

	/**
	 * <p>
	 * Return the Management Network Device {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is the Network Management {@link Node} related to the given
	 *            Instance {@link Node}, or <code>null</code>, if the given
	 *            Instance {@link Node} has no Network Management {@link Node}.
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if the Network Management Device Node Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node getNetworkManagementDevice(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		String sMgmtInterfaceSelector = findManagementNetworkDeviceSelector(mgmtNode);
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					sMgmtInterfaceSelector);
			if (nl != null && nl.getLength() > 1) {
				throw new ResourcesDescriptorException(instanceNode,
						Messages.NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE);
			} else if (nl == null || nl.getLength() == 0) {
				nl = GetHeritedContent.getHeritedContent(instanceNode,
						DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR);
			}
		} catch (XPathExpressionException Ex) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_SELECTOR,
					sMgmtInterfaceSelector), Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode,
					Messages.NetMgmtEx_NO_MGMT_NETWORK_DEVICE);
		}
		return nl.item(0);
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of {@link Node} which describes multiple
	 *            Instances.
	 * 
	 * @return a {@link list} of Management Network Device {@link Host} related
	 *         to the given {@link List} of Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if one Instance {@link Node} is not valid (ex : contains
	 *             invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if the Network Management Device Node Selector of one
	 *             Instance is not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a attribute equal to the Network Management
	 *             Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the value of one attribute is not a valid {@link Host}.
	 */
	public static List<Host> findNetworkManagementHost(List<Node> instanceNodes)
			throws ResourcesDescriptorException {
		List<Host> hl = new ArrayList<Host>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
				// raised when Network management datas are invalid.
				// in this situation, we will use default values
			}
			hl.add(getNetworkManagementHost(mgmtNode, instanceNode));
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
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Network Management Device Node Selector is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Network
	 *             Management Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host findNetworkManagementHost(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network management datas are invalid.
			// in this situation, we will use default values
		}
		return getNetworkManagementHost(mgmtNode, instanceNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is the Network Management {@link Node} related to the given
	 *            Instance {@link Node}, or <code>null</code>, if the given
	 *            Instance {@link Node} has no Network Management {@link Node}.
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device {@link Host}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Network Management Device Node Selector is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Network
	 *             Management Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host getNetworkManagementHost(Node mgmtNode, Node instanceNode)
			throws ResourcesDescriptorException {
		Node netNode = getNetworkManagementDevice(mgmtNode, instanceNode);
		String attr = findManagementNetworkDeviceAttribute(mgmtNode);
		String sHost = null;
		try {
			sHost = netNode.getAttributes().getNamedItem(attr).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_ATTRIBUTE,
					attr));
		}
		try {
			return Host.parseString(sHost);
		} catch (IllegalHostException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_PORT_ATTRIBUTE} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance doesn't have any Network Management
	 *             {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_PORT_ATTRIBUTE} can be found in
	 *             the given Instance's Network Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_PORT_ATTRIBUTE}
	 *             found in the given Instance's Network Management {@link Node}
	 *             is not a valid {@link Port}.
	 */
	public static Port findNetworkManagementPort(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getNetworkManagementPort(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}.
	 * 
	 * @return the Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_PORT_ATTRIBUTE} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Network Management {@link Node} is
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_PORT_ATTRIBUTE} can be found in
	 *             the given Network Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_PORT_ATTRIBUTE}
	 *             found in the given Network Management {@link Node} is not a
	 *             valid {@link Port}.
	 */
	public static Port getNetworkManagementPort(Node mgmtNode)
			throws ResourcesDescriptorException {
		String sPort = null;
		try {
			sPort = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_PORT_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					NETWORK_MGMT_PORT_ATTRIBUTE));
		}
		try {
			return Port.parseString(sPort);
		} catch (IllegalPortException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR,
					NETWORK_MGMT_PORT_ATTRIBUTE), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_METHOD_ATTRIBUTE} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance doesn't have any Network Management
	 *             {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_METHOD_ATTRIBUTE} can be found in
	 *             the given Instance's Network Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_METHOD_ATTRIBUTE}
	 *             found in the given Instance's Network Management {@link Node}
	 *             is not a valid {@link NetworkManagementMethod}.
	 */
	public static NetworkManagementMethod findNetworkManagementMethod(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getNetworkManagementMethod(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}.
	 * 
	 * @return the Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_METHOD_ATTRIBUTE} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Network Management {@link Node} is
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_METHOD_ATTRIBUTE} can be found in
	 *             the given Network Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_METHOD_ATTRIBUTE}
	 *             found in the given Network Management {@link Node} is not a
	 *             valid {@link NetworkManagementMethod}.
	 */
	public static NetworkManagementMethod getNetworkManagementMethod(
			Node mgmtNode) throws ResourcesDescriptorException {
		String sMethod = null;
		try {
			sMethod = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_METHOD_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					NETWORK_MGMT_METHOD_ATTRIBUTE));
		}
		try {
			return NetworkManagementMethod.parseString(sMethod);
		} catch (IllegalManagementMethodException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR,
					NETWORK_MGMT_METHOD_ATTRIBUTE), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Instance's Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_ENABLE_ATTRIBUTE} XML Attribute ;</li>
	 *         <li><code>false</code> if the given Instance has no Network
	 *         Management {@link Node} ;</li>
	 *         <li><code>true</code> if the given Instance's Network Management
	 *         {@link Node} has no {@link #NETWORK_MGMT_ENABLE_ATTRIBUTE} XML
	 *         Attribute ;</li>
	 *         </ul>
	 */
	public static boolean isNetworkManagementEnable(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			return false;
		}
		return getNetworkManagementEnable(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Network Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_ENABLE_ATTRIBUTE} XML Attribute ;</li>
	 *         <li><code>true</code> if the given Network Management
	 *         {@link Node} is <code>null</code> ;</li>
	 *         <li><code>true</code> if the given Network Management
	 *         {@link Node} has no {@link #NETWORK_MGMT_ENABLE_ATTRIBUTE} XML
	 *         Attribute ;</li>
	 *         </ul>
	 */
	public static boolean getNetworkManagementEnable(Node mgmtNode) {
		String sEnable = null;
		try {
			sEnable = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_ENABLE_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return true;
		}
		return Boolean.parseBoolean(sEnable);
	}

	/**
	 * <p>
	 * Return the Network Device Nodes Selector of the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute of
	 *         the given Instance's Network Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance has no Network Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance's Network Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 */
	public static String findNetworkDevicesSelector(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network management datas are invalid.
			// in this situation, we will use default values
		}
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR;
		}
	}

}
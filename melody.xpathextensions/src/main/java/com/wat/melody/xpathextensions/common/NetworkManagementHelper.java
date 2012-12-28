package com.wat.melody.xpathextensions.common;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.IllegalManagementMethodException;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

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
	 * The XML Element which contains Network Management datas in the RD
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
	 * Network Management Interface Nodes Selector
	 */
	public static final String NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE = "interfaceNodeSelector";

	/**
	 * The XML attribute of the Network Management Node, which contains the
	 * Network Management Interface Nodes Attribute Selector
	 */
	public static final String NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE = "interfaceNodeAttribute";

	/**
	 * XPath Expression to select Network Management Interface Node in Primary
	 * search
	 */
	public static final String DEFAULT_NETOWRK_MGMT_INTERFACE_NODE_SELECTOR = "//network//interface[@device='eth0']";

	/**
	 * XPath Expression to select Network Management Interface Node in Secondary
	 * search
	 */
	public static final String SECONDARY_NETOWRK_MGMT_INTERFACE_NODE_SELECTOR = "//network//interface";

	/**
	 * the XML attribute of the Network Management Interface Node which select
	 * the Host
	 */
	public static final String DEFAULT_NETWORK_MGMT_INTERFACE_ATTR_SELECTOR = "ip";

	/**
	 * <p>
	 * Return the Network Management {@link Node} related to the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Network Management {@link Node} related to the given Instance
	 *         {@link Node}.
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
	 *            is a Network Management {@link Node}.
	 * 
	 * @return the content of the
	 *         {@link #NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE} XML
	 *         Attribute of the given Network Management {@link Node} or
	 *         {@link #DEFAULT_NETOWRK_MGMT_INTERFACE_NODE_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE} XML
	 *         Attribute can be found.
	 */
	public static String findNetworkManagementInterfaceSelector(Node mgmtNode) {
		try {
			return mgmtNode
					.getAttributes()
					.getNamedItem(
							NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_MGMT_INTERFACE_NODE_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Network Management Interface Attribute Selector of the given
	 * Network Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Management {@link Node}.
	 * 
	 * @return the content of the
	 *         {@link #NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE} XML
	 *         Attribute of the given Network Management {@link Node} or
	 *         {@link #DEFAULT_NETWORK_MGMT_INTERFACE_ATTR_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE} XML
	 *         Attribute can be found.
	 */
	public static String findNetworkManagementInterfaceAttribute(Node mgmtNode) {
		try {
			return mgmtNode
					.getAttributes()
					.getNamedItem(
							NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETWORK_MGMT_INTERFACE_ATTR_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Network Management Interface {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Network Management Interface {@link Node} related to the
	 *         given Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Network Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Network Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if no Network Management Interface{@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Network Management Interface{@link Node} can be
	 *             found.
	 */
	public static Node findNetworkManagementInterface(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return getNetworkManagementInterface(mgmtNode, instanceNode);
	}

	/**
	 * <p>
	 * Return the Network Management Interface {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is the Network Management {@link Node} related to the given
	 *            Instance {@link Node} (can be null, if the given Instance
	 *            {@link Node} has no Network Management {@link Node}).
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Network Management Interface {@link Node} related to the
	 *         given Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Network Management Interface {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Network Management Interface {@link Node} can be
	 *             found.
	 */
	public static Node getNetworkManagementInterface(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		String sMgmtInterfaceSelector = findNetworkManagementInterfaceSelector(mgmtNode);
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					sMgmtInterfaceSelector);
			if (nl != null && nl.getLength() > 1) {
				throw new ResourcesDescriptorException(instanceNode,
						Messages.NetMgmtEx_TOO_MANY_MGMT_NETWORK_INTERFACE);
			} else if (nl == null || nl.getLength() == 0) {
				nl = GetHeritedContent.getHeritedContent(instanceNode,
						SECONDARY_NETOWRK_MGMT_INTERFACE_NODE_SELECTOR);
			}
		} catch (XPathExpressionException Ex) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_INTERFACE_SELECTOR,
					sMgmtInterfaceSelector), Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode,
					Messages.NetMgmtEx_NO_MGMT_NETWORK_INTERFACE);
		}
		return nl.item(0);
	}

	public static Host findNetworkManagementHost(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return getNetworkManagementHost(mgmtNode, instanceNode);
	}

	/**
	 * /!\ Will not fail if mgmtNode is null
	 * 
	 * @param mgmtNode
	 * @param instanceNode
	 * @return
	 * @throws ResourcesDescriptorException
	 */
	public static Host getNetworkManagementHost(Node mgmtNode, Node instanceNode)
			throws ResourcesDescriptorException {
		Node netNode = getNetworkManagementInterface(mgmtNode, instanceNode);
		String attr = findNetworkManagementInterfaceAttribute(mgmtNode);
		String sHost = null;
		try {
			sHost = netNode.getAttributes().getNamedItem(attr).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(
					netNode,
					Messages.bind(
							Messages.NetMgmtEx_INVALID_MGMT_NETWORK_INTERFACE_ATTRIBUTE,
							attr));
		}
		try {
			return Host.parseString(sHost);
		} catch (IllegalHostException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	public static Port findNetworkManagementPort(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getNetworkManagementPort(mgmtNode);
	}

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

	public static NetworkManagementMethod findNetworkManagementMethod(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getNetworkManagementMethod(mgmtNode);
	}

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

	public static boolean isNetworkManagementEnable(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getNetworkManagementEnable(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 * 
	 * @return <code>true</code> by default.
	 * 
	 * @throws ResourcesDescriptorException
	 */
	public static boolean getNetworkManagementEnable(Node mgmtNode)
			throws ResourcesDescriptorException {
		String sEnable = null;
		try {
			sEnable = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_ENABLE_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return true;
		}
		return Boolean.parseBoolean(sEnable);
	}

}

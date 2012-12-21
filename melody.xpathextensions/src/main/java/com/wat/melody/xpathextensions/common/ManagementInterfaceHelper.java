package com.wat.melody.xpathextensions.common;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.IllegalManagementMethodException;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class ManagementInterfaceHelper {

	/**
	 * The 'enableManagement' XML attribute to use in the sequence descriptor
	 */
	public static final String ENABLEMGNT_ATTR = "enableManagement";

	/**
	 * The 'enableManagementTimeout' XML attribute to use in the sequence
	 * descriptor
	 */
	public static final String ENABLEMGNT_TIMEOUT_ATTR = "enableManagementTimeout";

	/**
	 * The 'melody-management' XML Node in the RD
	 */
	public static final String MGMT_NODE = "melody-management";

	/**
	 * The 'method' XML attribute of the 'melody-management' XML Node
	 */
	public static final String MGMT_METHOD_ATTR = "method";

	/**
	 * The 'interfaceSelector' XML attribute of the 'melody-management' XML Node
	 */
	public static final String MGMT_NETWORK_INTERFACE_NODE_SELECTOR = "interfaceSelector";

	/**
	 * The 'interfaceAttribute' XML attribute of the 'melody-management' XML
	 * Node
	 */
	public static final String MGMT_NETWORK_INTERFACE_ATTR_SELECTOR = "interfaceAttribute";

	/**
	 * The 'port' XML attribute of the 'melody-management' XML Node
	 */
	public static final String MGMT_PORT_ATTR = "port";

	/**
	 * XPath Expression which selects Management Network Interface Node in
	 * Primary search
	 */
	public static final String DEFAULT_MGMT_NETOWRK_INTERFACE_NODE_SELECTOR = "//network//interface[@device='eth0']";

	/**
	 * XPath Expression which selects Management Network Interface Node in
	 * Secondary search
	 */
	public static final String SECONDARY_SEARCH = "//network//interface";

	/**
	 * the XML attribute of the Management Network Interface Node which select
	 * the Host
	 */
	public static final String DEFAULT_MGMT_NETWORK_INTERFACE_ATTR_SELECTOR = "ip";

	/**
	 * <p>
	 * Return the Melody-Management {@link Node} related to the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Melody-Management {@link Node} related to the given Instance
	 *         {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Melody-Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Melody-Management {@link Node} can be found.
	 */
	public static Node findMgmtNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode, "//"
					+ MGMT_NODE);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '//" + MGMT_NODE + "'. "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() > 1) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.MgmtEx_TOO_MANY_MGMT_NODE, MGMT_NODE));
		} else if (nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.MgmtEx_NO_MGMT_NODE, MGMT_NODE));
		}
		return nl.item(0);
	}

	/**
	 * <p>
	 * Return the Melody-Management Network Interface Node Selector of the given
	 * Melody-Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Melody-Management {@link Node}.
	 * 
	 * @return the content of the {@link #MGMT_NETWORK_INTERFACE_NODE_SELECTOR}
	 *         XML Attribute of the given Melody-Management {@link Node} or
	 *         {@link #DEFAULT_MGMT_NETOWRK_INTERFACE_NODE_SELECTOR} if no
	 *         {@link #MGMT_NETWORK_INTERFACE_NODE_SELECTOR} XML Attribute can
	 *         be found.
	 */
	public static String findMgmtInterfaceSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(MGMT_NETWORK_INTERFACE_NODE_SELECTOR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_NETOWRK_INTERFACE_NODE_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Melody-Management Network Interface Attribute Selector of the
	 * given Melody-Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Melody-Management {@link Node}.
	 * 
	 * @return the content of the {@link #MGMT_NETWORK_INTERFACE_ATTR_SELECTOR}
	 *         XML Attribute of the given Melody-Management {@link Node} or
	 *         {@link #DEFAULT_MGMT_NETWORK_INTERFACE_ATTR_SELECTOR} if no
	 *         {@link #MGMT_NETWORK_INTERFACE_ATTR_SELECTOR} XML Attribute can
	 *         be found.
	 */
	public static String findMgmtInterfaceAttribute(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(MGMT_NETWORK_INTERFACE_ATTR_SELECTOR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_NETWORK_INTERFACE_ATTR_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Melody-Management Network Interface {@link Node} related to
	 * the given Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Melody-Management Network Interface {@link Node} related to
	 *         the given Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Melody-Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Melody-Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if no Melody-Management Network Interface{@link Node} can be
	 *             found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Melody-Management Network Interface{@link Node}
	 *             can be found.
	 */
	public static Node getManagementNetworkInterface(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = ManagementInterfaceHelper.findMgmtNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when melody-management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return ManagementInterfaceHelper.getManagementNetworkInterface(
				mgmtNode, instanceNode);
	}

	/**
	 * <p>
	 * Return the Melody-Management Network Interface {@link Node} related to
	 * the given Instance {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is the Melody-Management {@link Node} related to the given
	 *            Instance {@link Node} (can be null, if the given Instance
	 *            {@link Node} has no Melody-Management {@link Node}).
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Melody-Management Network Interface {@link Node} related to
	 *         the given Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Melody-Management Network Interface{@link Node} can be
	 *             found.
	 * @throws ResourcesDescriptorException
	 *             if multiple Melody-Management Network Interface{@link Node}
	 *             can be found.
	 */
	public static Node getManagementNetworkInterface(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		String sMgmtInterfaceSelector = findMgmtInterfaceSelector(mgmtNode);
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					sMgmtInterfaceSelector);
			if (nl != null && nl.getLength() > 1) {
				throw new ResourcesDescriptorException(instanceNode,
						Messages.MgmtEx_TOO_MANY_MGMT_NETWORK_INTERFACE);
			} else if (nl == null || nl.getLength() == 0) {
				nl = GetHeritedContent.getHeritedContent(instanceNode,
						SECONDARY_SEARCH);
			}
		} catch (XPathExpressionException Ex) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.MgmtEx_INVALID_MGMT_NETWORK_INTERFACE_SELECTOR,
					sMgmtInterfaceSelector), Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode,
					Messages.MgmtEx_NO_MGMT_NETWORK_INTERFACE);
		}
		return nl.item(0);
	}

	public static Host getManagementNetworkInterfaceHost(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = ManagementInterfaceHelper.findMgmtNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when melody-management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return ManagementInterfaceHelper.getManagementNetworkInterfaceHost(
				mgmtNode, instanceNode);
	}

	/**
	 * /!\ Will not fail if mgmtNode is null
	 * 
	 * @param mgmtNode
	 * @param instanceNode
	 * @return
	 * @throws ResourcesDescriptorException
	 */
	public static Host getManagementNetworkInterfaceHost(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkInterface(mgmtNode, instanceNode);
		String attr = findMgmtInterfaceAttribute(mgmtNode);
		String sHost = null;
		try {
			sHost = netNode.getAttributes().getNamedItem(attr).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.MgmtEx_INVALID_MGMT_NETWORK_INTERFACE_ATTRIBUTE,
					attr));
		}
		try {
			return Host.parseString(sHost);
		} catch (IllegalHostException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	public static Port getManagementPort(Node mgmtNode)
			throws ResourcesDescriptorException {
		String sPort = null;
		try {
			sPort = mgmtNode.getAttributes().getNamedItem(MGMT_PORT_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_MISSING_ATTR, MGMT_PORT_ATTR));
		}
		try {
			return Port.parseString(sPort);
		} catch (IllegalPortException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, MGMT_PORT_ATTR), Ex);
		}
	}

	public static ManagementMethod getManagementMethod(Node mgmtNode)
			throws ResourcesDescriptorException {
		String sMethod = null;
		try {
			sMethod = mgmtNode.getAttributes().getNamedItem(MGMT_METHOD_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_MISSING_ATTR, MGMT_METHOD_ATTR));
		}
		try {
			return ManagementMethod.parseString(sMethod);
		} catch (IllegalManagementMethodException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, MGMT_METHOD_ATTR), Ex);
		}
	}

}

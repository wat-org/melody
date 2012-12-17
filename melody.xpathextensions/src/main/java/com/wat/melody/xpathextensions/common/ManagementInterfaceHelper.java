package com.wat.melody.xpathextensions.common;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class ManagementInterfaceHelper {

	/*
	 * TODO : il y en a un peu dans com.wat.melody.cloud.management, un peu ici
	 * ... c'est degeulasse
	 * 
	 * => refactor de com.wat.melody.cloud.management, pour qu'il s'appuye
	 * entierement sur cette classe ?
	 */

	public static final String DEFAULT_MGMT_NETOWRK_INTERFACE_SELECTOR = "//network//interface[@device='eth0']";
	public static final String DEFAULT_MGMT_NETWORK_INTERFACE_ATTRIBUTE = "ip";
	public static final String SECONDARY_SEARCH = "//network//interface";

	/**
	 * The 'melody-management' XML Node in the RD
	 */
	public static final String MGMT_NODE = "melody-management";

	/**
	 * The 'interfaceSelector' XML attribute of the 'melody-management' XML Node
	 */
	public static final String MGMT_NETWORK_INTERFACE_SELECTOR = "interfaceSelector";

	/**
	 * The 'interfaceAttribute' XML attribute of the 'melody-management' XML
	 * Node
	 */
	public static final String MGMT_NETWORK_INTERFACE_ATTR = "interfaceAttribute";

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

	public static String findMgmtInterfaceSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(MGMT_NETWORK_INTERFACE_SELECTOR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_NETOWRK_INTERFACE_SELECTOR;
		}
	}

	public static String findMgmtInterfaceAttribute(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(MGMT_NETWORK_INTERFACE_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_NETWORK_INTERFACE_ATTRIBUTE;
		}
	}

	public static Node getManagementNetworkInterfaceNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = ManagementInterfaceHelper.findMgmtNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when melody-management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return ManagementInterfaceHelper.getManagementNetworkInterfaceNode(
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
	public static Node getManagementNetworkInterfaceNode(Node mgmtNode,
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

	public static String getManagementNetworkInterfaceHost(Node instanceNode)
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
	public static String getManagementNetworkInterfaceHost(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkInterfaceNode(mgmtNode, instanceNode);
		String attr = findMgmtInterfaceAttribute(mgmtNode);
		try {
			return netNode.getAttributes().getNamedItem(attr).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.MgmtEx_INVALID_MGMT_NETWORK_INTERFACE_ATTRIBUTE,
					attr));
		}
	}

	public static Node getManagementNetworkInterfaceHostNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = ManagementInterfaceHelper.findMgmtNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when melody-management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		return ManagementInterfaceHelper.getManagementNetworkInterfaceHostNode(
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
	public static Node getManagementNetworkInterfaceHostNode(Node mgmtNode,
			Node instanceNode) throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkInterfaceNode(mgmtNode, instanceNode);
		String attr = findMgmtInterfaceAttribute(mgmtNode);
		try {
			return netNode.getAttributes().getNamedItem(attr);
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.MgmtEx_INVALID_MGMT_NETWORK_INTERFACE_ATTRIBUTE,
					attr));
		}
	}

}

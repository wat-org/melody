package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.xpathextensions.common.Messages;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public final class GetManagementInterface implements XPathFunction {

	public static final String NAME = "getManagementInterface";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node)) {
			throw new IllegalArgumentException(arg0.getClass()
					.getCanonicalName()
					+ ": Not accepted. "
					+ CustomXPathFunctions.NAMESPACE
					+ ":"
					+ NAME
					+ "() expects a Node " + "argument.");
		}
		try {
			return getManagementNetworkInterfaceNode((Node) arg0);
		} catch (ResourcesDescriptorException Ex) {
			/*
			 * TODO : add the location of the Node in the error message
			 */
			throw new XPathFunctionException(Ex);
		}
	}

	/*
	 * TODO : il y en a un peu dans com.wat.melody.cloud.management, un peu ici
	 * ... c'est degeulasse
	 * 
	 * => refactor de com.wat.melody.cloud.management, pour qu'il s'appuye
	 * entierement sur cette classe ?
	 */

	/*
	 * TODO : creer une methode qui recupere l'ip directement (et pas le node)
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
			mgmtNode = findMgmtNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when melody-management datas are invalid.
			// in this situation, we consider eth0 is the management interface
		}
		// /!\ Will not fail if mgmtNode is null
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

}

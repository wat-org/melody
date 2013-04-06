package com.wat.melody.cloud.network;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.IllegalManagementMethodNetworkException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.FilteredDocHelper;

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
	public static final String NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "networkDevicesSelector";

	/**
	 * Default XPath Expression to select Network Devices.
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR = "//"
			+ NetworkDeviceNamesLoader.INTERFACE_NE;

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * criteria of XPath Expression to select Network Device Management Node.
	 */
	public static final String NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR = "mgmtNetworkDeviceCriteria";

	/**
	 * Default XPath Expression to select Network Device Management Node
	 */
	public static final String DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA = "@"
			+ NetworkDeviceNamesLoader.DEVICE_ATTR + "='eth0'";

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * XML attribute of the Network Device Management Node which select the Host
	 * to manage.
	 */
	public static final String NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR = "mgmtNetworkDeviceAttribute";

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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if any Instance {@link Node} has no Network Device Management
	 *             {@link Node}.
	 */
	public static List<Node> findNetworkManagementNode(List<Node> instanceNodes)
			throws ResourcesDescriptorException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Node> hl = new ArrayList<Node>();
		for (Node instanceNode : instanceNodes) {
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Network Device Management {@link Node} can be found.
	 */
	public static Node findNetworkManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
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
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_NO_MGMT_NODE, NETWORK_MGMT_NE));
		} else if (nl.getLength() > 1) {
			return nl.item(nl.getLength() - 1);
		}
		return nl.item(0);
	}

	/**
	 * <p>
	 * Return the Management Network Device Selector of the given Network Device
	 * Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}, or
	 *            <code>null</code>.
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
	public static String getManagementNetworkDeviceSelector(Node mgmtNode) {
		String sCriteria = null;
		try {
			sCriteria = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR)
					.getNodeValue();
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
	 *            is a Network Device Management {@link Node}, or
	 *            <code>null</code>.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute of the given Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if the
	 *         given Network Device Management {@link Node} was
	 *         <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute can be found ;</li>
	 *         </ul>
	 */
	public static String getManagementNetworkDeviceAttributeSelector(
			Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR)
					.getNodeValue();
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of an Instance
	 *             {@link Node} is not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if any Instance {@link Node} has no Management Network Device
	 *             {@link Node}.
	 */
	public static List<Node> findManagementNetworkDeviceNode(
			List<Node> instanceNodes) throws ResourcesDescriptorException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Node> hl = new ArrayList<Node>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node findManagementNetworkDeviceNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node getManagementNetworkDeviceNode(Node instanceNode,
			Node mgmtNode) throws ResourcesDescriptorException {
		NodeList nl = null;
		String sMgmtInterfaceSelector = getManagementNetworkDeviceSelector(mgmtNode);
		try {
			nl = Doc.evaluateAsNodeList("." + sMgmtInterfaceSelector,
					instanceNode);
			if (nl != null && nl.getLength() > 1) {
				throw new ResourcesDescriptorException(instanceNode,
						Messages.NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE);
			} else if (nl == null || nl.getLength() == 0) {
				sMgmtInterfaceSelector = getNetworkDevicesSelector(mgmtNode);
				nl = Doc.evaluateAsNodeList("." + sMgmtInterfaceSelector,
						instanceNode);
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
	 *            is a {@link List} of Instance {@link Node}.
	 * 
	 * @return a {@link list} of Management Network Device {@link Host} related
	 *         to the given {@link List} of Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a attribute equal to the Network Device
	 *             Management Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the value of one attribute is not a valid {@link Host}.
	 */
	public static List<Host> findManagementNetworkHost(List<Node> instanceNodes)
			throws ResourcesDescriptorException {
		List<Host> hl = new ArrayList<Host>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host findManagementNetworkHost(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device {@link Host}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host getManagementNetworkHost(Node instanceNode, Node mgmtNode)
			throws ResourcesDescriptorException {
		try {
			return Host.parseString(getManagementNetworkHostNode(instanceNode,
					mgmtNode).getNodeValue());
		} catch (NullPointerException Ex) {
			Node netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalHostException Ex) {
			Node netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
			throw new ResourcesDescriptorException(netNode, Messages.bind(
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 */
	public static List<Node> findManagementNetworkHostNode(
			List<Node> instanceNodes) throws ResourcesDescriptorException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Node> hl = new ArrayList<Node>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			Node n = getManagementNetworkHostNode(instanceNode, mgmtNode);
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node findManagementNetworkHostNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device {@link Host}
	 *         {@link Node}, or <tt>null</tt> if the given Instance doesn't have
	 *         one.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node getManagementNetworkHostNode(Node instanceNode,
			Node mgmtNode) throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkDeviceNode(instanceNode, mgmtNode);
		String attr = getManagementNetworkDeviceAttributeSelector(mgmtNode);
		return netNode.getAttributes().getNamedItem(attr);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management {@link Port}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#PORT_ATTR} attribute
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link Port}.
	 * @throws ResourcesDescriptorException
	 *             if the {@link ManagementNetworkDatasLoader#PORT_ATTR}
	 *             attribute is not defined in the given Network Device
	 *             Management {@link Node} and the given Network Device
	 *             Management {@link Node} doesn't define a
	 *             ManagementNetworkMethod (which is normaly used to define a
	 *             default port).
	 */
	public static Port findManagementNetworkPort(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#PORT_ATTR} attribute
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link Port}.
	 * @throws ResourcesDescriptorException
	 *             if the {@link ManagementNetworkDatasLoader#PORT_ATTR}
	 *             attribute is not defined in the given Network Device
	 *             Management {@link Node} and the given Network Device
	 *             Management {@link Node} doesn't define a
	 *             ManagementNetworkMethod (which is normaly used to define a
	 *             default port).
	 */
	public static Port getManagementNetworkPort(Node mgmtNode)
			throws ResourcesDescriptorException {
		try {
			return Port.parseString(getManagementNetworkPortNode(mgmtNode)
					.getNodeValue());
		} catch (NullPointerException Ex) {
			return getDefaultMamangementPort(mgmtNode);
		} catch (IllegalPortException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
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
	 *         as a {@link Node}, or <code>null</code> if it doesn't have one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Node findManagementNetworkPortNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkPortNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link Port} attribute as a
	 *         {@link Node}, or <code>null</code> if such attribute cannot be
	 *         found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <code>null</code>.
	 */
	public static Node getManagementNetworkPortNode(Node mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		return mgmtNode.getAttributes().getNamedItem(
				ManagementNetworkDatasLoader.PORT_ATTR);
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the given Network Device Management {@link Node} doesn't
	 *             define a ManagementNetworkMethod (which is normaly used to
	 *             define a default port).
	 */
	private static Port getDefaultMamangementPort(Node mgmtNode)
			throws ResourcesDescriptorException {
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
		} catch (ResourcesDescriptorException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws ResourcesDescriptorException
	 *             if no {@link ManagementNetworkDatasLoader#METHOD_ATTR} can be
	 *             found in the given Instance's Network Device Management
	 *             {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#METHOD_ATTR} found in the
	 *             given Instance's Network Device Management {@link Node} is
	 *             not a valid {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod findManagementNetworkMethod(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no {@link ManagementNetworkDatasLoader#METHOD_ATTR} can be
	 *             found in the given Network Device Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#METHOD_ATTR} found in the
	 *             given Network Device Management {@link Node} is not a valid
	 *             {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod getManagementNetworkMethod(
			Node mgmtNode) throws ResourcesDescriptorException {
		try {
			return ManagementNetworkMethod
					.parseString(getManagementNetworkMethodNode(mgmtNode)
							.getNodeValue());
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR,
					ManagementNetworkDatasLoader.METHOD_ATTR));
		} catch (IllegalManagementMethodNetworkException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
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
	 *         <code>null</code> it doesn't have one.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Node findManagementNetworkMethodNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkMethodNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link ManagementNetworkMethod}
	 *         Attribute as a {@link Node}, or <code>null</code> if such
	 *         attribute cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <code>null</code>.
	 */
	public static Node getManagementNetworkMethodNode(Node mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		return mgmtNode.getAttributes().getNamedItem(
				ManagementNetworkDatasLoader.METHOD_ATTR);
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 */
	public static boolean isManagementNetworkEnable(Node instanceNode) {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *         {@link Node} is <code>null</code> ;</li>
	 *         <li><code>true</code> if the given Network Device Management
	 *         {@link Node} has no
	 *         {@link ManagementNetworkDatasLoader#ENABLE_ATTR} XML Attribute ;</li>
	 *         </ul>
	 */
	public static boolean getManagementNetworkEnable(Node mgmtNode) {
		if (mgmtNode == null) {
			return false;
		}
		String attr = ManagementNetworkDatasLoader.ENABLE_ATTR;
		try {
			return Boolean.parseBoolean(mgmtNode.getAttributes()
					.getNamedItem(attr).getNodeValue());
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 */
	public static String findNetworkDevicesSelector(Node instanceNode) {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *         Network Device Management {@link Node} is <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Network Device Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 */
	public static String getNetworkDevicesSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if any Network Devices Selector (found in the Network Device
	 *             Management {@link Node} of the instance) is not a valid XPath
	 *             Expression.
	 */
	public static List<Node> findNetworkDeviceNodeByName(
			List<Node> instanceNodes, String netDevName)
			throws ResourcesDescriptorException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Node> hl = new ArrayList<Node>();
		for (Node instanceNode : instanceNodes) {
			NodeList nl = findNetworkDeviceNodeByName(instanceNode, netDevName);
			for (int i = 0; i < nl.getLength(); i++) {
				hl.add(nl.item(i));
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node} of the given instance) is not a valid
	 *             XPath Expression.
	 */
	public static NodeList findNetworkDeviceNodeByName(Node instanceNode,
			String netDevName) throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of the given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given name.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList getNetworkDeviceNodeByName(Node instanceNode,
			Node mgmtNode, String netDevName)
			throws ResourcesDescriptorException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance Node.");
		}
		String sAllNetDevSelector = getNetworkDevicesSelector(mgmtNode);
		String sNetDevSelector = "." + sAllNetDevSelector
				+ (netDevName == null ? "" : "[@device='" + netDevName + "']");
		try {
			return Doc.evaluateAsNodeList(sNetDevSelector, instanceNode);
		} catch (XPathExpressionException Ex) {
			Node attr = mgmtNode.getAttributes().getNamedItem(
					NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a 'device' attribute.
	 * @throws ResourcesDescriptorException
	 *             if the value of one attribute is not a valid
	 *             {@link NetworkDeviceName}.
	 */
	public static List<NetworkDeviceName> findManagementNetworkDeviceName(
			List<Node> instanceNodes) throws ResourcesDescriptorException {
		List<NetworkDeviceName> ndl = new ArrayList<NetworkDeviceName>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a 'device' attribute.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link NetworkDeviceName}.
	 */
	public static NetworkDeviceName findManagementNetworkDeviceName(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device
	 *             {@link NetworkDeviceName} doesn't have a 'device' attribute.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link NetworkDeviceName}.
	 */
	public static NetworkDeviceName getManagementNetworkDeviceName(
			Node instanceNode, Node mgmtNode)
			throws ResourcesDescriptorException {
		try {
			return NetworkDeviceName
					.parseString(getManagementNetworkDeviceNameNode(
							instanceNode, mgmtNode).getNodeValue());
		} catch (NullPointerException Ex) {
			Node netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = NetworkDeviceNamesLoader.DEVICE_ATTR;
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr), Ex);
		} catch (IllegalNetworkDeviceNameException Ex) {
			Node netNode = getManagementNetworkDeviceNode(instanceNode,
					mgmtNode);
			String attr = NetworkDeviceNamesLoader.DEVICE_ATTR;
			throw new ResourcesDescriptorException(netNode, Messages.bind(
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
	 *             <code>null</code> or contains <tt>null</tt> elements.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 */
	public static List<Node> findManagementNetworkDeviceNameNode(
			List<Node> instanceNodes) throws ResourcesDescriptorException {
		if (instanceNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List of Instance Node.");
		}
		List<Node> hl = new ArrayList<Node>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			Node n = getManagementNetworkDeviceNameNode(instanceNode, mgmtNode);
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node findManagementNetworkDeviceNameNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device
	 *         {@link NetworkDeviceName} {@link Node}, or <tt>null</tt> if it
	 *         doens't have one.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node getManagementNetworkDeviceNameNode(Node instanceNode,
			Node mgmtNode) throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkDeviceNode(instanceNode, mgmtNode);
		String attr = NetworkDeviceNamesLoader.DEVICE_ATTR;
		return netNode.getAttributes().getNamedItem(attr);
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
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#ENABLE_TIMEOUT_ATTR}
	 *             found in the given Instance's Network Device Management
	 *             {@link Node} is not a valid
	 *             {@link ManagementNetworkEnableTimeout}.
	 */
	public static ManagementNetworkEnableTimeout findManagementNetworkEnableTimeout(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
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
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link ManagementNetworkDatasLoader#ENABLE_TIMEOUT_ATTR}
	 *             found in the given Network Device Management {@link Node} is
	 *             not a valid {@link ManagementNetworkEnableTimeout}.
	 */
	public static ManagementNetworkEnableTimeout getManagementNetworkEnableTimeout(
			Node mgmtNode) throws ResourcesDescriptorException {
		Node n = getManagementNetworkEnableTimeoutNode(mgmtNode);
		if (n == null) {
			return getDefaultManagementEnableTimeout(mgmtNode);
		}
		try {
			return ManagementNetworkEnableTimeout.parseString(n.getNodeValue());
		} catch (IllegalTimeoutException Ex) {
			String attr = ManagementNetworkDatasLoader.ENABLE_TIMEOUT_ATTR;
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management enable-timeout attribute
	 *         as a {@link Node}, or <code>null</code> if no enable-timeout
	 *         attribute can be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 */
	public static Node findManagementNetworkEnableTimeoutNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkEnableTimeoutNode(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management enable-timeout attribute as a
	 *         {@link Node}, or <code>null</code> if no enable-timeout attribute
	 *         can be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Network Device Management {@link Node} is
	 *             <code>null</code>.
	 */
	public static Node getManagementNetworkEnableTimeoutNode(Node mgmtNode) {
		if (mgmtNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Network Device Management Node.");
		}
		String attr = ManagementNetworkDatasLoader.ENABLE_TIMEOUT_ATTR;
		return mgmtNode.getAttributes().getNamedItem(attr);
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
			Node mgmtNode) {
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
		} catch (ResourcesDescriptorException Ex) {
			return ManagementNetworkDatas.DEFAULT_ENABLE_TIMEOUT;
		}
	}

}
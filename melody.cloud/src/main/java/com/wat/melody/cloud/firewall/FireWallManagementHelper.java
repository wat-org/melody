package com.wat.melody.cloud.firewall;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FireWallManagementHelper {

	/**
	 * XML Element in the RD, which contains FireWall Management datas of the
	 * related Instance Node (more formally called the
	 * "FireWall Management Node")
	 */
	public static final String FIREWALL_MGMT_NE = "firewall-management";

	/**
	 * XPath Expression which select the FireWall Management Node of the related
	 * Instance Node
	 */
	public static final String FIREWALL_MGMT_NODE_SELECTOR = "//"
			+ FIREWALL_MGMT_NE;

	/**
	 * XML attribute of the FireWall Management Node, which contains the
	 * FireWall Rule Nodes Selector
	 */
	public static final String FIREWALL_RULE_NODES_SELECTOR_ATTRIBUTE = "FireWallRulesSelector";

	/**
	 * Default XPath Expression to select FireWall Rules Nodes in the RD,
	 * related to an Instance Node
	 */
	public static final String DEFAULT_FIREWALL_RULE_NODES_SELECTOR = "//"
			+ FwRuleLoader.FIREWALL_RULE_NE;

	/**
	 * <p>
	 * Return the FireWall Management {@link Node} related to the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return <ul>
	 *         <li>The Disk Management {@link Node} related to the given
	 *         Instance {@link Node}, if one Disk Management {@link Node} is
	 *         found ;</li>
	 *         <li>The last Disk Management {@link Node} related to the given
	 *         Instance {@link Node}, if multiple Disk Management {@link Node}
	 *         were found ;</li>
	 *         <li><code>null</code>, if no Disk Management {@link Node} were
	 *         found ;</li>
	 *         </ul>
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 */
	public static Node findFireWallManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = XPathHelper.getHeritedContent(instanceNode,
					FIREWALL_MGMT_NODE_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '" + FIREWALL_MGMT_NODE_SELECTOR
					+ "'. " + "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() > 1) {
			return nl.item(nl.getLength() - 1);
		} else if (nl.getLength() == 0) {
			return null;
		}
		return nl.item(0);
	}

	public static String findFireWallRulesSelector(Node instanceNode) {
		Node mgmtNode = null;
		try {
			mgmtNode = findFireWallManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(FIREWALL_RULE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_FIREWALL_RULE_NODES_SELECTOR;
		}
	}

}

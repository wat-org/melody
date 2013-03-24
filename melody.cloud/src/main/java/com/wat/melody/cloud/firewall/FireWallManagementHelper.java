package com.wat.melody.cloud.firewall;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.xml.FilteredDocHelper;

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
			+ FireWallRulesLoader.FIREWALL_RULE_NE;

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
	 *         <li>The FireWall Management {@link Node} related to the given
	 *         Instance {@link Node}, if one FireWall Management {@link Node} is
	 *         found ;</li>
	 *         <li>The last FireWall Management {@link Node} related to the
	 *         given Instance {@link Node}, if multiple FireWall Management
	 *         {@link Node} were found ;</li>
	 *         <li><code>null</code>, if no FireWall Management {@link Node}
	 *         were found ;</li>
	 *         </ul>
	 */
	public static Node findFireWallManagementNode(Node instanceNode) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceNode,
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
		Node mgmtNode = findFireWallManagementNode(instanceNode);
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(FIREWALL_RULE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_FIREWALL_RULE_NODES_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the FireWall Rule {@link Node}s of the given Instance {@link Node}
	 * .
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return The FireWall Rule {@link Node}s of the given Instance
	 *         {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the FireWall Rules Selector (found in the FireWall Rule
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList findFireWallRules(Node instanceNode)
			throws ResourcesDescriptorException {
		String sAllFWRulesSelector = findFireWallRulesSelector(instanceNode);
		try {
			return FilteredDocHelper.getHeritedContent(instanceNode,
					sAllFWRulesSelector);
		} catch (XPathExpressionException Ex) {
			Node mgmtNode = findFireWallManagementNode(instanceNode);
			Node attr = mgmtNode.getAttributes().getNamedItem(
					FIREWALL_RULE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
					Messages.FWRulesMgmtEx_INVALID_FWRULES_SELECTOR,
					sAllFWRulesSelector), Ex);
		}
	}

}

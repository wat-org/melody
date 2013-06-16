package com.wat.melody.cloud.firewall.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.firewall.Messages;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FireWallRulesHelper {

	/**
	 * XML Element, which contains FireWall Management datas, related to an
	 * Instance Element (more formally called the
	 * "FireWall Management Element").
	 */
	public static final String FIREWALL_MGMT_ELEMENT = "firewall-management";

	/**
	 * XPath Expression which select the FireWall Management Element, related to
	 * an Instance Element.
	 */
	public static final String FIREWALL_MGMT_ELEMENT_SELECTOR = "//"
			+ FIREWALL_MGMT_ELEMENT;

	/**
	 * XML attribute of the FireWall Management Element, which contains the
	 * XPath Expression to select TCP FireWall Rules Elements.
	 */
	public static final String TCP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE = "tcp-firewall-rules-selector";

	/**
	 * XML attribute of the FireWall Management element, which contains the
	 * XPath Expression to select TCP FireWall Rules Elements.
	 */
	public static final String UDP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE = "udp-firewall-rules-selector";

	/**
	 * XML attribute of the FireWall Management Element, which contains the
	 * XPath Expression to select TCP FireWall Rules Elements.
	 */
	public static final String ICMP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE = "icmp-firewall-rules-selector";

	/**
	 * Default XPath Expression to select TCP FireWall Rules Elements, related
	 * to an Instance Element.
	 */
	public static final String DEFAULT_TCP_RULE_ELEMENTS_SELECTOR = "//"
			+ TcpFireWallRulesLoader.DEFAULT_TCP_FIREWALL_RULE_ELEMENT;

	/**
	 * Default XPath Expression to select UDP FireWall Rules Elements, related
	 * to an Instance Element.
	 */
	public static final String DEFAULT_UDP_RULE_ELEMENTS_SELECTOR = "//"
			+ UdpFireWallRulesLoader.DEFAULT_UDP_FIREWALL_RULE_ELEMENT;

	/**
	 * Default XPath Expression to select ICMP FireWall Rules Elements, related
	 * to an Instance Element.
	 */
	public static final String DEFAULT_ICMP_RULE_ELMENTS_SELECTOR = "//"
			+ IcmpFireWallRulesLoader.DEFAULT_ICMP_FIREWALL_RULE_ELEMENT;

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the FireWall Management Element related to the given Instance
	 *         {@link Element}, which is :
	 *         <ul>
	 *         <li>The last FireWall Management Element related to the given
	 *         Instance {@link Element}, if FireWall Management Elements are
	 *         found ;</li>
	 *         <li><tt>null</tt>, if no FireWall Management Element are found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 */
	public static Element findFireWallManagementElement(Element instanceElmt) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt,
					FIREWALL_MGMT_ELEMENT_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '"
					+ FIREWALL_MGMT_ELEMENT_SELECTOR + "'. "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() == 0) {
			return null;
		}
		// Conversion can't fail: the expression can only return Element
		return (Element) nl.item(nl.getLength() - 1);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a FireWall Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no FireWall Management Element.
	 * 
	 * @return the TCP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is <tt>null</tt> ;</li>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is not <tt>null</tt> but has no
	 *         Custom TCP FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom TCP FireWall Rules Selector defined in the given
	 *         FireWall Management Element ;</li>
	 *         </ul>
	 */
	public static String getTcpFireWallRuleElementsSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(
					TCP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_TCP_RULE_ELEMENTS_SELECTOR;
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return all TCP FireWall Rule {@link Element}s of the given Instance. Can
	 *         be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom TCP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom TCP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findTcpFireWallRules(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = findFireWallManagementElement(instanceElmt);
		String selector = getTcpFireWallRuleElementsSelector(mgmtElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(TCP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.TcpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(TCP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.TcpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a FireWall Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no FireWall Management Element.
	 * 
	 * @return the UPD FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is <tt>null</tt> ;</li>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is not <tt>null</tt> but has no
	 *         Custom UDP FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom UDP FireWall Rules Selector defined in the given
	 *         FireWall Management Element ;</li>
	 *         </ul>
	 */
	public static String getUdpFireWallRuleElementsSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(
					UDP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_UDP_RULE_ELEMENTS_SELECTOR;
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return all UDP FireWall Rule {@link Element}s of the given Instance. Can
	 *         be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom UDP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom UDP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findUdpFireWallRules(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = findFireWallManagementElement(instanceElmt);
		String selector = getUdpFireWallRuleElementsSelector(mgmtElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(UDP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.UdpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(UDP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.UdpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a FireWall Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no FireWall Management Element.
	 * 
	 * @return the ICMP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is <tt>null</tt> ;</li>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELMENTS_SELECTOR}, if the given
	 *         FireWall Management Element is not <tt>null</tt> but has no
	 *         Custom ICMP FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom ICMP FireWall Rules Selector defined in the given
	 *         FireWall Management Element ;</li>
	 *         </ul>
	 */
	public static String getIcmpFireWallRuleElementsSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(
					ICMP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_ICMP_RULE_ELMENTS_SELECTOR;
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return all ICMP FireWall Rule {@link Element}s of the given Instance.
	 *         Can be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom ICMP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom ICMP FireWall Rules Selector (found in the
	 *             given Instance's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findIcmpFireWallRules(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = findFireWallManagementElement(instanceElmt);
		String selector = getIcmpFireWallRuleElementsSelector(mgmtElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(ICMP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.IcmpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(ICMP_RULE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.IcmpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

}
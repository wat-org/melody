package com.wat.melody.cloud.firewall.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.firewall.Messages;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.ProtectedAreaNames;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.cloud.protectedarea.xml.ProtectedAreaHelper;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;
import com.wat.melody.xpathextensions.XPathHelper;

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
	public static final String TCP_RULE_ELEMENTS_SELECTOR = "tcp-firewall-rules-selector";

	/**
	 * XML attribute of the FireWall Management element, which contains the
	 * XPath Expression to select TCP FireWall Rules Elements.
	 */
	public static final String UDP_RULE_ELEMENTS_SELECTOR = "udp-firewall-rules-selector";

	/**
	 * XML attribute of the FireWall Management Element, which contains the
	 * XPath Expression to select TCP FireWall Rules Elements.
	 */
	public static final String ICMP_RULE_ELEMENTS_SELECTOR = "icmp-firewall-rules-selector";

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
	public static final String DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR = "//"
			+ IcmpFireWallRulesLoader.DEFAULT_ICMP_FIREWALL_RULE_ELEMENT;

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the TCP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom TCP
	 *         FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom TCP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getTcpFireWallRuleElementsSelector(Element instanceElmt) {
		try {
			return XPathHelper.getHeritedAttributeValue(instanceElmt,
					"/" + FIREWALL_MGMT_ELEMENT + "/@"
							+ TCP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_TCP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		} catch (NodeRelatedException e) {
			throw new RuntimeException("cannot contains an xpath expression.");
		}
	}

	private static Attr getTcpFireWallRuleElementsSelectorAttr(
			Element instanceElmt) {
		try {
			return FilteredDocHelper.getHeritedAttribute(instanceElmt,
					"/" + FIREWALL_MGMT_ELEMENT + "/@"
							+ TCP_RULE_ELEMENTS_SELECTOR, null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
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
		String selector = getTcpFireWallRuleElementsSelector(instanceElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getTcpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.TcpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getTcpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.TcpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the UPD FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom UDP
	 *         FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom UDP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getUdpFireWallRuleElementsSelector(Element instanceElmt) {
		try {
			return XPathHelper.getHeritedAttributeValue(instanceElmt,
					"/" + FIREWALL_MGMT_ELEMENT + "/@"
							+ UDP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_UDP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		} catch (NodeRelatedException e) {
			throw new RuntimeException("cannot contains an xpath expression.");
		}
	}

	private static Attr getUdpFireWallRuleElementsSelectorAttr(
			Element instanceElmt) {
		try {
			return FilteredDocHelper.getHeritedAttribute(instanceElmt,
					"/" + FIREWALL_MGMT_ELEMENT + "/@"
							+ UDP_RULE_ELEMENTS_SELECTOR, null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
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
		String selector = getUdpFireWallRuleElementsSelector(instanceElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getUdpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.UdpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getUdpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.UdpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the ICMP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom
	 *         ICMP FireWall Rules Selector is defined in ;</li>
	 *         <li>The Custom ICMP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getIcmpFireWallRuleElementsSelector(
			Element instanceElmt) {
		try {
			return XPathHelper.getHeritedAttributeValue(instanceElmt, "/"
					+ FIREWALL_MGMT_ELEMENT + "/@"
					+ ICMP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		} catch (NodeRelatedException e) {
			throw new RuntimeException("cannot contains an xpath expression.");
		}
	}

	private static Attr getIcmpFireWallRuleElementsSelectorAttr(
			Element instanceElmt) {
		try {
			return FilteredDocHelper.getHeritedAttribute(instanceElmt, "/"
					+ FIREWALL_MGMT_ELEMENT + "/@"
					+ ICMP_RULE_ELEMENTS_SELECTOR, null);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
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
		String selector = getIcmpFireWallRuleElementsSelector(instanceElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getIcmpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.IcmpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getIcmpFireWallRuleElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.IcmpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	public static String ADDRESSES_SEPARATOR = ",";

	/**
	 * @param e
	 *            is the instance or protected area {@link Element} where the
	 *            firewall rules are defined.
	 * @param addrattr
	 *            is the {@link Attr} of a firewall rule of the given
	 *            {@link Element} where the given addresses are defined).
	 * @param addresses
	 *            is a CSV <tt>String</tt>, which contains {@link Address} (the
	 *            value of the given {@link Attr}). Can be <tt>null</tt>.
	 * 
	 * @return an {@link Address} set, build from the given CSV <tt>String</tt>.
	 *         Each part of the given CSV input <tt>String</tt> can be either an
	 *         {@link IpRange} or a {@link ProtectedAreaName}. Each
	 *         {@link ProtectedAreaName} will be converted in their
	 *         corresponding {@link ProtectedAreaId}.
	 * 
	 * @throws NodeRelatedException
	 *             if a part of the given <tt>String</tt> is neither an
	 *             {@link IpRange} nor a {@link ProtectedAreaName}, or if a
	 *             {@link ProtectedAreaName} cannot be converted to a
	 *             {@link ProtectedAreaId}.
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} or the given {@link Attr} is
	 *             <tt>null</tt>.
	 */
	public static Addresses parseAddresses(Element e, Attr addrattr,
			String addresses) throws NodeRelatedException {
		if (e == null) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		if (addrattr == null) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid " + Attr.class.getCanonicalName() + ".");
		}
		Addresses res = new Addresses();
		if (addresses == null) {
			return res;
		}
		for (String address : addresses.split(ADDRESSES_SEPARATOR)) {
			address = address.trim();
			if (address.length() == 0) {
				continue;
			}
			ConsolidatedException cex = new ConsolidatedException();
			// try to convert to IpRange
			Address addr = null;
			try {
				addr = IpRange.parseString(address);
				res.add(addr);
				// conversion ok: deal with next entry
				continue;
			} catch (IllegalIpRangeException Ex) {
				cex.addCause(Ex);
			}
			// try to convert to ProtectedAreaName
			ProtectedAreaName paname = null;
			try {
				paname = ProtectedAreaName.parseString(address);
			} catch (IllegalProtectedAreaNameException Ex) {
				cex.addCause(Ex);
				throw new NodeRelatedException(addrattr,
						"Cannot be converted to a valid address.", cex);
			}
			// try to convert to ProtectedAreaName
			ProtectedAreaIds paids = null;
			// first, get the region
			String region = null;
			try {
				region = XPathHelper.getHeritedAttributeValue(e, "/@"
						+ InstanceDatasLoader.REGION_ATTR, null);
			} catch (XPathExpressionException bug) {
				throw new RuntimeException("Because the XPath Expression "
						+ "is hard-coded, such error cannot happened. "
						+ "There must be a bug somewhere.", bug);
			}
			// then, convert
			try {
				paids = ProtectedAreaHelper.convertProtectedAreaFromNamesToIds(
						e, new ProtectedAreaNames(paname), region);
			} catch (Exception Ex) {
				cex.addCause(new Exception(paname + ": Not accepted. "
						+ "Such Protected Area is not valid.", Ex));
				throw new NodeRelatedException(addrattr,
						"Cannot be converted to a valid address.", cex);
			}
			// conversion ok
			res.add(paids.iterator().next());
		}
		return res;
	}

}
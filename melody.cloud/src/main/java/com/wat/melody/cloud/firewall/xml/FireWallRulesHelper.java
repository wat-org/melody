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
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;
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
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return the TCP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_TCP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom TCP
	 *         FireWall Rules Selector defined in ;</li>
	 *         <li>The Custom TCP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getTcpFireWallRuleElementsSelector(Element e) {
		return getTcpFireWallRuleElementsSelectorAttr(e).getValue();
	}

	private static Attr getTcpFireWallRuleElementsSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./" + FIREWALL_MGMT_ELEMENT
					+ "/@" + TCP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_TCP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return all TCP FireWall Rule {@link Element}s of the given element. Can
	 *         be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom TCP FireWall Rules Selector (found in the
	 *             given elment's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom TCP FireWall Rules Selector (found in the
	 *             given element's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findTcpFireWallRules(Element e)
			throws NodeRelatedException {
		String selector = getTcpFireWallRuleElementsSelector(e);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, e);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getTcpFireWallRuleElementsSelectorAttr(e),
					Msg.bind(Messages.TcpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getTcpFireWallRuleElementsSelectorAttr(e), Msg.bind(
							Messages.TcpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return the UPD FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_UDP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom UDP
	 *         FireWall Rules Selector defined in ;</li>
	 *         <li>The Custom UDP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getUdpFireWallRuleElementsSelector(Element e) {
		return getUdpFireWallRuleElementsSelectorAttr(e).getValue();
	}

	private static Attr getUdpFireWallRuleElementsSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./" + FIREWALL_MGMT_ELEMENT
					+ "/@" + UDP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_UDP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return all UDP FireWall Rule {@link Element}s of the given element. Can
	 *         be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom UDP FireWall Rules Selector (found in the
	 *             given element's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom UDP FireWall Rules Selector (found in the
	 *             given element's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findUdpFireWallRules(Element e)
			throws NodeRelatedException {
		String selector = getUdpFireWallRuleElementsSelector(e);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, e);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getUdpFireWallRuleElementsSelectorAttr(e),
					Msg.bind(Messages.UdpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getUdpFireWallRuleElementsSelectorAttr(e), Msg.bind(
							Messages.UdpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return the ICMP FireWall Rules Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has no FireWall Management Element ;</li>
	 *         <li>{@link #DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR}, if the given
	 *         element has a FireWall Management Element which has no Custom
	 *         ICMP FireWall Rules Selector defined in ;</li>
	 *         <li>The Custom ICMP FireWall Rules Selector defined in the given
	 *         element's FireWall Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getIcmpFireWallRuleElementsSelector(Element e) {
		return getIcmpFireWallRuleElementsSelectorAttr(e).getValue();
	}

	private static Attr getIcmpFireWallRuleElementsSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./" + FIREWALL_MGMT_ELEMENT
					+ "/@" + ICMP_RULE_ELEMENTS_SELECTOR,
					DEFAULT_ICMP_RULE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return all ICMP FireWall Rule {@link Element}s of the given element. Can
	 *         be an empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom ICMP FireWall Rules Selector (found in the
	 *             given element's FireWall Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom ICMP FireWall Rules Selector (found in the
	 *             given element's FireWall Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findIcmpFireWallRules(Element e)
			throws NodeRelatedException {
		String selector = getIcmpFireWallRuleElementsSelector(e);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, e);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getIcmpFireWallRuleElementsSelectorAttr(e), Msg.bind(
							Messages.IcmpMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getIcmpFireWallRuleElementsSelectorAttr(e), Msg.bind(
							Messages.IcmpMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

	public static String ADDRESSES_SEPARATOR = ",";

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area, where the firewall rules are defined.
	 * @param addrattr
	 *            is the {@link Attr} of a firewall rule of the given
	 *            {@link Element} where the given addresses are defined.
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
				region = DocHelper.getAttributeValue(e, "./@"
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
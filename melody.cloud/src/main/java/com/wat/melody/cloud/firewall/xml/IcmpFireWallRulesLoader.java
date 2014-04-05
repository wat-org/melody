package com.wat.melody.cloud.firewall.xml;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexIcmpFireWallRule;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.IcmpCodes;
import com.wat.melody.common.firewall.IcmpTypes;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.firewall.exception.IllegalIcmpCodesException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypesException;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpFireWallRulesLoader extends AbstractFireWallRulesLoader {

	/**
	 * The default value of the XML Nested element of an Instance Element, which
	 * contains the definition of an ICMP FireWall Rule.
	 */
	public static final String DEFAULT_ICMP_FIREWALL_RULE_ELEMENT = "icmp";

	/**
	 * XML attribute of a FireWall Rule Element, which define its icmp types.
	 */
	public static final String TYPES_ATTR = "types";

	/**
	 * XML attribute of a FireWall Rule Element, which define its icmp codes.
	 */
	public static final String CODES_ATTR = "codes";

	protected IcmpTypes loadIcmpTypes(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TYPES_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IcmpTypes.parseString(v);
		} catch (IllegalIcmpTypesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(e, TYPES_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected IcmpCodes loadIcmpCodes(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, CODES_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IcmpCodes.parseString(v);
		} catch (IllegalIcmpCodesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(e, CODES_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the ICMP FireWall Rule {@link Element}s of the given Instance
	 * {@link Element} and convert it into a {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * An ICMP FireWall Rule {@link Element} may have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRefs} ;</li>
	 * <li>from-ips : which should contains {@link Addresses} ;</li>
	 * <li>to-ips : which should contains {@link Addresses} :</li>
	 * <li>codes : which should contains {@link IcmpTypes} ;</li>
	 * <li>types : which should contains {@link IcmpCodes} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>access : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Element}'s attribute is not valid).
	 */
	public FireWallRulesPerDevice load(Element instanceElmt)
			throws NodeRelatedException {
		List<Element> icmpFireRuleElmts = FireWallRulesHelper
				.findIcmpFireWallRules(instanceElmt);

		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		for (Element icmpFireRuleElmt : icmpFireRuleElmts) {
			Directions dirs = loadDirection(icmpFireRuleElmt);
			Addresses fromAddresses = loadFromIps(icmpFireRuleElmt,
					instanceElmt);
			Addresses toAddresses = loadToIps(icmpFireRuleElmt, instanceElmt);
			if (fromAddresses == null && dirs.contains(Direction.IN)) {
				if (dirs.contains(Direction.OUT)) {
					dirs.remove(Direction.IN);
				} else {
					continue;
				}
			}
			if (toAddresses == null && dirs.contains(Direction.OUT)) {
				if (dirs.contains(Direction.IN)) {
					dirs.remove(Direction.OUT);
				} else {
					continue;
				}
			}
			IcmpTypes types = loadIcmpTypes(icmpFireRuleElmt);
			IcmpCodes codes = loadIcmpCodes(icmpFireRuleElmt);
			NetworkDeviceNameRefs refs = loadNetworkDeviceNameRefs(icmpFireRuleElmt);
			Access access = loadAccess(icmpFireRuleElmt);
			fwrs.merge(refs, new ComplexIcmpFireWallRule(fromAddresses,
					toAddresses, types, codes, dirs, access));
		}
		return fwrs;
	}

}
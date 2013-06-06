package com.wat.melody.cloud.firewall;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import com.wat.melody.common.network.IpRanges;
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
	 * XML attribute of a FwRule Element Node, which define the icmp type of the
	 * icmp Fw Rule.
	 */
	public static final String TYPES_ATTR = "types";

	/**
	 * XML attribute of a FwRule Element Node, which define the icmp code of the
	 * icmp Fw Rule.
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
	 * An ICMP FireWall Rule {@link Element} must have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRefs} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>codes : which should contains {@link IcmpTypes} ;</li>
	 * <li>types : which should contains {@link IcmpCodes} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>allow : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an Instance {@link Element}.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Element}'s attribute is not valid, or the 'herit' XML
	 *             attribute is not valid).
	 */
	public FireWallRulesPerDevice load(Element instanceElmt)
			throws NodeRelatedException {
		NodeList nl = FireWallManagementHelper
				.findIcmpFireWallRules(instanceElmt);

		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			Directions dirs = loadDirection(e);
			IpRanges fromIps = loadFromIps(e);
			IpRanges toIps = loadToIps(e);
			if (fromIps == null && dirs.contains(Direction.IN)) {
				if (dirs.contains(Direction.OUT)) {
					dirs.remove(Direction.IN);
				} else {
					continue;
				}
			}
			if (toIps == null && dirs.contains(Direction.OUT)) {
				if (dirs.contains(Direction.IN)) {
					dirs.remove(Direction.OUT);
				} else {
					continue;
				}
			}
			IcmpTypes types = loadIcmpTypes(e);
			IcmpCodes codes = loadIcmpCodes(e);
			NetworkDeviceNameRefs refs = loadNetworkDeviceNameRefs(e);
			Access access = loadAccess(e);
			fwrs.merge(refs, new ComplexIcmpFireWallRule(fromIps, toIps, types,
					codes, dirs, access));
		}
		return fwrs;
	}

}
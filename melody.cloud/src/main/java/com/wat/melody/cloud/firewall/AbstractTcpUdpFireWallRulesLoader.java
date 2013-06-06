package com.wat.melody.cloud.firewall;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexFireWallRule;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTcpUdpFireWallRulesLoader extends
		AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FwRule Element Node, which define the source ports of
	 * the Fw Rule.
	 */
	public static final String FROM_PORTS_ATTR = "from-ports";

	/**
	 * XML attribute of a FwRule Element Node, which define the destination
	 * ports of the Fw Rule.
	 */
	public static final String TO_PORTS_ATTR = "to-ports";

	protected PortRanges loadFromPorts(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, FROM_PORTS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return PortRanges.parseString(v);
		} catch (IllegalPortRangesException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					FROM_PORTS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected PortRanges loadToPorts(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TO_PORTS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return PortRanges.parseString(v);
		} catch (IllegalPortRangesException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e, TO_PORTS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the TCP/UDP FireWall Rule {@link Element}s of the given Instance
	 * {@link Element} and convert it into a {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * A TCP/UDP FireWall Rule {@link Element} must have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRefs} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
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
		NodeList nl = findFwRuleNodes(instanceElmt);

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
			PortRanges fromPorts = loadFromPorts(e);
			PortRanges toPorts = loadToPorts(e);
			NetworkDeviceNameRefs refs = loadNetworkDeviceNameRefs(e);
			Access access = loadAccess(e);
			fwrs.merge(refs,
					newFwRule(fromIps, fromPorts, toIps, toPorts, dirs, access));
		}
		return fwrs;
	}

	public abstract NodeList findFwRuleNodes(Element instanceElmt)
			throws NodeRelatedException;

	public abstract ComplexFireWallRule newFwRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access);

}
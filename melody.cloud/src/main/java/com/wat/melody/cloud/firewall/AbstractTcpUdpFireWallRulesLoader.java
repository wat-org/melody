package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexFireWallRule;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceNameRef;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTcpUdpFireWallRulesLoader extends
		AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FwRule Node, which define the source ports of the Fw
	 * Rule.
	 */
	public static final String FROM_PORTS_ATTR = "from-ports";

	/**
	 * XML attribute of a FwRule Node, which define the destination ports of the
	 * Fw Rule.
	 */
	public static final String TO_PORTS_ATTR = "to-ports";

	protected PortRanges loadFromPorts(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, FROM_PORTS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return PortRanges.parseString(v);
		} catch (IllegalPortRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					FROM_PORTS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	protected PortRanges loadToPorts(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TO_PORTS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return PortRanges.parseString(v);
		} catch (IllegalPortRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, TO_PORTS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the TCP/UDP FireWall Rule {@link Node}s of the given Instance
	 * {@link Node} and convert it into a {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * <i>A TCP/UDP FireWall Rule <code>Node</code> must have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRef} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>allow : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another FireWall Rule <code>Node</code>, which attributes will be used as
	 * source ;</li>
	 * </ul>
	 * </i>
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code> or is
	 *             not an element {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Node}'s attribute is not valid, or the 'herit' XML
	 *             attribute is not valid).
	 */
	public FireWallRulesPerDevice load(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = findFwRuleNodes(instanceNode);

		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		Node n = null;
		for (int i = 0; i < nl.getLength(); i++) {
			n = nl.item(i);
			Directions dirs = loadDirection(n);
			IpRanges fromIps = loadFromIps(n);
			IpRanges toIps = loadToIps(n);
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
			PortRanges fromPorts = loadFromPorts(n);
			PortRanges toPorts = loadToPorts(n);
			NetworkDeviceNameRefs refs = loadNetworkDeviceNameRefs(n);
			Access access = loadAccess(n);
			fwrs.merge(refs,
					newFwRule(fromIps, fromPorts, toIps, toPorts, dirs, access));
		}
		return fwrs;
	}

	public abstract NodeList findFwRuleNodes(Node instanceNode)
			throws ResourcesDescriptorException;

	public abstract ComplexFireWallRule newFwRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access);

}
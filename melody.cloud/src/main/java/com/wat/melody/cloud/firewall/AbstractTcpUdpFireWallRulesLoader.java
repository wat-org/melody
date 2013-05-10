package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FwRule;
import com.wat.melody.common.firewall.FwRules;
import com.wat.melody.common.firewall.Interfaces;
import com.wat.melody.common.firewall.exception.IllegalAccessException;
import com.wat.melody.common.firewall.exception.IllegalDirectionsException;
import com.wat.melody.common.firewall.exception.IllegalInterfacesException;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTcpUdpFireWallRulesLoader {

	/**
	 * XML attribute of a FwRule Node, which define the name of the device to
	 * attache the Fw Rule to.
	 */
	public static final String DEVICES_NAME_ATTR = "devices-name";

	/**
	 * XML attribute of a FwRule Node, which define the source ips of the Fw
	 * Rule.
	 */
	public static final String FROM_IPS_ATTR = "from-ips";

	/**
	 * XML attribute of a FwRule Node, which define the source ports of the Fw
	 * Rule.
	 */
	public static final String FROM_PORTS_ATTR = "from-ports";

	/**
	 * XML attribute of a FwRule Node, which define the destination ips of the
	 * Fw Rule.
	 */
	public static final String TO_IPS_ATTR = "to-ips";

	/**
	 * XML attribute of a FwRule Node, which define the destination ports of the
	 * Fw Rule.
	 */
	public static final String TO_PORTS_ATTR = "to-ports";

	/**
	 * XML attribute of a FwRule Node, which define the directions of the flow.
	 */
	public static final String DIRECTIONS_ATTR = "directions";

	/**
	 * XML attribute of a FwRule Node, which define the action to perform.
	 */
	public static final String ACCESS_ATTR = "access";

	public AbstractTcpUdpFireWallRulesLoader() {
	}

	private IpRanges loadFromIps(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, FROM_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, FROM_IPS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private PortRanges loadFromPorts(Node n)
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

	private IpRanges loadToIps(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TO_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, TO_IPS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private PortRanges loadToPorts(Node n) throws ResourcesDescriptorException {
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

	private Interfaces loadDevices(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DEVICES_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Interfaces.parseString(v);
		} catch (IllegalInterfacesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					DEVICES_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Directions loadDirection(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DIRECTIONS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Directions.parseString(v);
		} catch (IllegalDirectionsException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					DIRECTIONS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Access loadAccess(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, ACCESS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Access.parseString(v);
		} catch (IllegalAccessException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, ACCESS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the TCP/UDP FireWall Rule {@link Node}s of the given Instance
	 * {@link Node} and convert it into a {@link FwRules}.
	 * </p>
	 * 
	 * <p>
	 * <i>A TCP/UDP FireWall Rule <code>Node</code> must have the attributes :
	 * <ul>
	 * <li>device-name : which should contains {@link NetworkDeviceName} ;</li>
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
	 * @return a {@link FwRules} object, which is a collection of {@link FwRule}
	 *         .
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code> or is
	 *             not an element {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Node}'s attribute is not valid, or the 'herit' XML
	 *             attribute is not valid).
	 */
	public FwRules load(Node instanceNode) throws ResourcesDescriptorException {
		NodeList nl = findFwRuleNodes(instanceNode);

		FwRules fwrs = new FwRules();
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
			Interfaces inters = loadDevices(n);
			Access access = loadAccess(n);
			fwrs.add(newFwRule(inters, fromIps, fromPorts, toIps, toPorts,
					dirs, access));
		}
		return fwrs;
	}

	public abstract NodeList findFwRuleNodes(Node instanceNode)
			throws ResourcesDescriptorException;

	public abstract FwRule newFwRule(Interfaces interfaces,
			IpRanges fromIpRanges, PortRanges fromPortRanges,
			IpRanges toIpRanges, PortRanges toPortRanges,
			Directions directions, Access access);

}
package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.IcmpCodes;
import com.wat.melody.common.firewall.IcmpTypes;
import com.wat.melody.common.firewall.NetworkDeviceNameRef;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesLoader {

	/**
	 * <p>
	 * Find the FireWall Rule {@link Node}s of the given Instance {@link Node}
	 * and convert it into a {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * A FireWall Rule {@link Node} can describe TCP, UDP or ICMP FireWall Rule.
	 * </p>
	 * <p>
	 * A TCP and UDP FireWall Rule {@link Node} must have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRef} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>allow : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another FireWall Rule {@link Node}, which attributes will be used as
	 * source ;</li>
	 * </ul>
	 * </p>
	 * <p>
	 * An ICMP FireWall Rule {@link Node} must have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRef} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>codes : which should contains {@link IcmpTypes} ;</li>
	 * <li>types : which should contains {@link IcmpCodes} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>allow : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another FireWall Rule {@link Node}, which attributes will be used as
	 * source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object. .
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
		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		fwrs.merge(new TcpFireWallRulesLoader().load(instanceNode));
		fwrs.merge(new UdpFireWallRulesLoader().load(instanceNode));
		fwrs.merge(new IcmpFireWallRulesLoader().load(instanceNode));
		return fwrs;
	}

}

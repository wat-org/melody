package com.wat.melody.cloud.firewall.xml;

import org.w3c.dom.Element;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.IcmpCodes;
import com.wat.melody.common.firewall.IcmpTypes;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesLoader {

	/**
	 * <p>
	 * Find the FireWall Rule {@link Element}s of the given Instance
	 * {@link Element} or a Protected Area {@link Element} and convert it into a
	 * {@link FireWallRulesPerDevice}.
	 * </p>
	 * 
	 * <p>
	 * A FireWall Rule {@link Element} can describe TCP, UDP or ICMP FireWall
	 * Rule.
	 * </p>
	 * <p>
	 * A TCP and UDP FireWall Rule {@link Element} may have the attributes :
	 * <ul>
	 * <li>devices-name : which should contains {@link NetworkDeviceNameRefs} ;</li>
	 * <li>from-ips : which should contains {@link Addresses} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link Addresses} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
	 * <li>directions : which should contains {@link Directions} ;</li>
	 * <li>access : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
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
	 * @param e
	 *            is an {@link Element} which describes an Instance or a
	 *            Protected Area.
	 * 
	 * @return a {@link FireWallRulesPerDevice} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a FireWall Rule
	 *             {@link Element}'s attribute is not valid).
	 */
	public FireWallRulesPerDevice load(Element e) throws NodeRelatedException {
		FireWallRulesPerDevice fwrs = new FireWallRulesPerDevice();
		fwrs.merge(new TcpFireWallRulesLoader().load(e));
		fwrs.merge(new UdpFireWallRulesLoader().load(e));
		fwrs.merge(new IcmpFireWallRulesLoader().load(e));
		return fwrs;
	}

}
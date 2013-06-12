package com.wat.melody.cloud.firewall.xml;

import java.util.List;

import org.w3c.dom.Element;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexFireWallRule;
import com.wat.melody.common.firewall.ComplexUdpFireWallRule;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UdpFireWallRulesLoader extends AbstractTcpUdpFireWallRulesLoader {

	/**
	 * The default value of the XML Nested element of an Instance Element, which
	 * contains the definition of an UDP FireWall Rule.
	 */
	public static final String DEFAULT_UDP_FIREWALL_RULE_ELEMENT = "udp";

	@Override
	public List<Element> findFwRuleNodes(Element instanceElmt)
			throws NodeRelatedException {
		return FireWallRulesHelper.findUdpFireWallRules(instanceElmt);
	}

	@Override
	public ComplexFireWallRule newFwRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		return new ComplexUdpFireWallRule(fromIpRanges, fromPortRanges,
				toIpRanges, toPortRanges, directions, access);
	}

}
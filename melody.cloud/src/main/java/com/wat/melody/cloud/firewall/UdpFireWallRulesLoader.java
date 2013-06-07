package com.wat.melody.cloud.firewall;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

	@Override
	public NodeList findFwRuleNodes(Element instanceElmt)
			throws NodeRelatedException {
		return FireWallManagementHelper.findUdpFireWallRules(instanceElmt);
	}

	@Override
	public ComplexFireWallRule newFwRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		return new ComplexUdpFireWallRule(fromIpRanges, fromPortRanges,
				toIpRanges, toPortRanges, directions, access);
	}

}
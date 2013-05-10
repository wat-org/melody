package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FwRule;
import com.wat.melody.common.firewall.Interfaces;
import com.wat.melody.common.firewall.UdpFwRule;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UdpFireWallRulesLoader extends AbstractTcpUdpFireWallRulesLoader {

	@Override
	public NodeList findFwRuleNodes(Node instanceNode)
			throws ResourcesDescriptorException {
		return FireWallManagementHelper.findUdpFireWallRules(instanceNode);
	}

	@Override
	public FwRule newFwRule(Interfaces interfaces, IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		return new UdpFwRule(interfaces, fromIpRanges, fromPortRanges,
				toIpRanges, toPortRanges, directions, access);
	}

}

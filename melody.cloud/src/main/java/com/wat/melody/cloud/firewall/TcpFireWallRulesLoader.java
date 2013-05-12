package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.ComplexFireWallRule;
import com.wat.melody.common.firewall.ComplexTcpFireWallRule;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TcpFireWallRulesLoader extends AbstractTcpUdpFireWallRulesLoader {

	@Override
	public NodeList findFwRuleNodes(Node instanceNode)
			throws ResourcesDescriptorException {
		return FireWallManagementHelper.findTcpFireWallRules(instanceNode);
	}

	@Override
	public ComplexFireWallRule newFwRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		return new ComplexTcpFireWallRule(fromIpRanges, fromPortRanges,
				toIpRanges, toPortRanges, directions, access);
	}

}
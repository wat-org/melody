package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ComplexUdpFireWallRule extends ComplexAbstractTcpUdpFireWallRule {

	public ComplexUdpFireWallRule(IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		super(fromIpRanges, fromPortRanges, toIpRanges, toPortRanges,
				directions, access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.UDP;
	}

	@Override
	public SimpleFireWallRule newFwRuleDecomposed(IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIpRange, PortRange toPortRange,
			Direction direction, Access access) {
		return new SimpleUdpFireWallRule(fromIpRange, fromPortRange, toIpRange,
				toPortRange, direction, access);
	}

}
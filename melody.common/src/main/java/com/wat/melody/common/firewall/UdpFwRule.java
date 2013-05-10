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
public class UdpFwRule extends AbstractTcpUdpFwRule {

	public UdpFwRule(Interfaces interfaces, IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		super(interfaces, fromIpRanges, fromPortRanges, toIpRanges,
				toPortRanges, directions, access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.UDP;
	}

	@Override
	public FwRuleDecomposed newFwRuleDecomposed(Interface inter,
			IpRange fromIpRange, PortRange fromPortRange, IpRange toIpRange,
			PortRange toPortRange, Direction direction, Access access) {
		return new UdpFwRuleDecomposed(inter, fromIpRange, fromPortRange,
				toIpRange, toPortRange, direction, access);
	}

}
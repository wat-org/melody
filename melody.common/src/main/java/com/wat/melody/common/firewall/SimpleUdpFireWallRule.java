package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleUdpFireWallRule extends SimpleAbstractTcpUdpFireWallwRule {

	public SimpleUdpFireWallRule(IpRange fromIpRange, PortRange fromPortRange,
			IpRange toIpRange, PortRange toPortRange, Direction direction,
			Access access) {
		super(fromIpRange, fromPortRange, toIpRange, toPortRange, direction,
				access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.UDP;
	}

}
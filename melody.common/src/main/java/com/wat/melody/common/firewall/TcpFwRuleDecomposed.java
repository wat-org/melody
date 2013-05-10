package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TcpFwRuleDecomposed extends AbstractTcpUdpFwRuleDecomposed {

	public TcpFwRuleDecomposed(Interface inter, IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIpRange, PortRange toPortRange,
			Direction direction, Access access) {
		super(inter, fromIpRange, fromPortRange, toIpRange, toPortRange,
				direction, access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.TCP;
	}

}
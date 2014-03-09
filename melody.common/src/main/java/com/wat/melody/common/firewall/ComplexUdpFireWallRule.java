package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ComplexUdpFireWallRule extends ComplexAbstractTcpUdpFireWallRule {

	public ComplexUdpFireWallRule(Addresses fromAddresses,
			PortRanges fromPortRanges, Addresses toAddresses,
			PortRanges toPortRanges, Directions directions, Access access) {
		super(fromAddresses, fromPortRanges, toAddresses, toPortRanges,
				directions, access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.UDP;
	}

	@Override
	public SimpleFireWallRule newSimpleFireWallRule(Address fromAddress,
			PortRange fromPortRange, Address toAddress, PortRange toPortRange,
			Direction direction, Access access) {
		return new SimpleUdpFireWallRule(fromAddress, fromPortRange, toAddress,
				toPortRange, direction, access);
	}

}
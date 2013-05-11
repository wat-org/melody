package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTcpUdpFwRuleDecomposed extends
		AbstractFwRuleDecomposed {

	private static PortRange DEFAULT_FROM_PORT_RANGE = PortRange.ALL;
	private static PortRange DEFAULT_TO_PORT_RANGE = PortRange.ALL;

	private PortRange moFromPortRange = DEFAULT_FROM_PORT_RANGE;
	private PortRange moToPortRange = DEFAULT_TO_PORT_RANGE;

	public AbstractTcpUdpFwRuleDecomposed(Interface inter, IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIpRange, PortRange toPortRange,
			Direction direction, Access access) {
		super(inter, fromIpRange, toIpRange, direction, access);
		setFromPortRange(fromPortRange);
		setToPortRange(toPortRange);
	}

	@Override
	public int hashCode() {
		return getFromIpRange().hashCode() + getFromPortRange().hashCode()
				+ getToIpRange().hashCode() + getToPortRange().hashCode();
	}

	@Override
	public String toString() {
		return "{ " + "device-name: " + getInterface() + ", protocol: "
				+ getProtocol() + ", from-ip: " + getFromIpRange()
				+ ", from-port: " + getFromPortRange() + ", to-ip: "
				+ getToIpRange() + ", to-port: " + getToPortRange()
				+ ", direction: " + getDirection() + ", access: " + getAccess()
				+ " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof AbstractTcpUdpFwRuleDecomposed) {
			AbstractTcpUdpFwRuleDecomposed rule = (AbstractTcpUdpFwRuleDecomposed) anObject;
			return rule.getFromIpRange().equals(getFromIpRange())
					&& rule.getFromPortRange().equals(getFromPortRange())
					&& rule.getToIpRange().equals(getToIpRange())
					&& rule.getToPortRange().equals(getToPortRange())
					&& rule.getProtocol().equals(getProtocol())
					&& rule.getDirection().equals(getDirection())
					&& rule.getAccess().equals(getAccess())
					&& (rule.getInterface().equals(Interface.ALL)
							|| getInterface().equals(Interface.ALL) || rule
							.getInterface().equals(getInterface()));
		}
		return false;
	}

	public PortRange getFromPortRange() {
		return moFromPortRange;
	}

	public PortRange setFromPortRange(PortRange fromPortRange) {
		if (fromPortRange == null) {
			fromPortRange = DEFAULT_FROM_PORT_RANGE;
		}
		PortRange previous = getFromPortRange();
		moFromPortRange = fromPortRange;
		return previous;
	}

	public PortRange getToPortRange() {
		return moToPortRange;
	}

	public PortRange setToPortRange(PortRange toPortRange) {
		if (toPortRange == null) {
			toPortRange = DEFAULT_TO_PORT_RANGE;
		}
		PortRange previous = getToPortRange();
		moToPortRange = toPortRange;
		return previous;
	}

}
package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SimpleAbstractTcpUdpFireWallwRule extends
		SimpleAbstractFireWallRule {

	private static PortRange DEFAULT_FROM_PORT_RANGE = PortRange.ALL;
	private static PortRange DEFAULT_TO_PORT_RANGE = PortRange.ALL;

	private PortRange _fromPortRange = DEFAULT_FROM_PORT_RANGE;
	private PortRange _toPortRange = DEFAULT_TO_PORT_RANGE;

	public SimpleAbstractTcpUdpFireWallwRule(IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIpRange, PortRange toPortRange,
			Direction direction, Access access) {
		super(fromIpRange, toIpRange, direction, access);
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
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol : ");
		str.append(getProtocol());
		str.append(", from-ips : ");
		str.append(getFromIpRange());
		str.append(", from-ports : ");
		str.append(getFromPortRange());
		str.append(", to-ips : ");
		str.append(getToIpRange());
		str.append(", to-ports : ");
		str.append(getToPortRange());
		str.append(", directions : ");
		str.append(getDirection());
		str.append(", access : ");
		str.append(getAccess());
		str.append(" }");
		return str.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof SimpleAbstractTcpUdpFireWallwRule) {
			SimpleAbstractTcpUdpFireWallwRule rule = (SimpleAbstractTcpUdpFireWallwRule) anObject;
			return rule.getFromIpRange().equals(getFromIpRange())
					&& rule.getFromPortRange().equals(getFromPortRange())
					&& rule.getToIpRange().equals(getToIpRange())
					&& rule.getToPortRange().equals(getToPortRange())
					&& rule.getProtocol().equals(getProtocol())
					&& rule.getDirection().equals(getDirection())
					&& rule.getAccess().equals(getAccess());
		}
		return false;
	}

	public PortRange getFromPortRange() {
		return _fromPortRange;
	}

	public PortRange setFromPortRange(PortRange fromPortRange) {
		if (fromPortRange == null) {
			fromPortRange = DEFAULT_FROM_PORT_RANGE;
		}
		PortRange previous = getFromPortRange();
		_fromPortRange = fromPortRange;
		return previous;
	}

	public PortRange getToPortRange() {
		return _toPortRange;
	}

	public PortRange setToPortRange(PortRange toPortRange) {
		if (toPortRange == null) {
			toPortRange = DEFAULT_TO_PORT_RANGE;
		}
		PortRange previous = getToPortRange();
		_toPortRange = toPortRange;
		return previous;
	}

}
package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
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

	public SimpleAbstractTcpUdpFireWallwRule(Address fromAddress,
			PortRange fromPortRange, Address toAddress, PortRange toPortRange,
			Direction direction, Access access) {
		super(fromAddress, toAddress, direction, access);
		setFromPortRange(fromPortRange);
		setToPortRange(toPortRange);
	}

	@Override
	public int hashCode() {
		return getFromAddress().hashCode() + getFromPortRange().hashCode()
				+ getToAddress().hashCode() + getToPortRange().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol: ");
		str.append(getProtocol());
		str.append(", from-ips: ");
		str.append(getFromAddress());
		str.append(", from-ports: ");
		str.append(getFromPortRange());
		str.append(", to-ips: ");
		str.append(getToAddress());
		str.append(", to-ports: ");
		str.append(getToPortRange());
		str.append(", directions: ");
		str.append(getDirection());
		str.append(", access: ");
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
			return rule.getFromAddress().equals(getFromAddress())
					&& rule.getFromPortRange().equals(getFromPortRange())
					&& rule.getToAddress().equals(getToAddress())
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
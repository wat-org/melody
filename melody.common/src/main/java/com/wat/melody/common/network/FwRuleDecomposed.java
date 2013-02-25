package com.wat.melody.common.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRuleDecomposed {

	private static Interface DEFAULT_INTERFACE = Interface.ALL;
	private static IpRange DEFAULT_FROM_IP_RANGE = IpRange.ALL;
	private static PortRange DEFAULT_FROM_PORT_RANGE = PortRange.ALL;
	private static IpRange DEFAULT_TO_IP_RANGE = IpRange.ALL;
	private static PortRange DEFAULT_TO_PORT_RANGE = PortRange.ALL;
	private static Protocol DEFAULT_PROTOCOL = Protocol.TCP;
	private static Direction DEFAULT_DIRECTION = Direction.IN;
	private static Access DEFAULT_ACCESS = Access.DENY;

	/*
	 * TODO : remove the device
	 */
	private Interface moInterface = DEFAULT_INTERFACE;
	private IpRange moFromIpRange = DEFAULT_FROM_IP_RANGE;
	private PortRange moFromPortRange = DEFAULT_FROM_PORT_RANGE;
	private IpRange moToIpRange = DEFAULT_TO_IP_RANGE;
	private PortRange moToPortRange = DEFAULT_TO_PORT_RANGE;
	private Protocol meProtocol = DEFAULT_PROTOCOL;
	private Direction meDirection = DEFAULT_DIRECTION;
	private Access meAccess = DEFAULT_ACCESS;

	public FwRuleDecomposed(Interface inter, IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIpRange, PortRange toPortRange,
			Protocol protocol, Direction direction, Access access) {
		setInterface(inter);
		setFromIpRange(fromIpRange);
		setFromPortRange(fromPortRange);
		setToIpRange(toIpRange);
		setToPortRange(toPortRange);
		setProtocol(protocol);
		setDirection(direction);
		setAccess(access);
	}

	@Override
	public int hashCode() {
		return getFromIpRange().hashCode() + getFromPortRange().hashCode()
				+ getToIpRange().hashCode() + getToPortRange().hashCode();
	}

	@Override
	public String toString() {
		return "{ " + "devive-name: " + getInterface() + ", from-ip: "
				+ getFromIpRange() + ", from-port: " + getFromPortRange()
				+ ", to-ip: " + getToIpRange() + ", to-port: "
				+ getToPortRange() + ", protocol: " + getProtocol()
				+ ", direction: " + getDirection() + ", access: " + getAccess()
				+ " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof FwRuleDecomposed) {
			FwRuleDecomposed rule = (FwRuleDecomposed) anObject;
			return rule.getFromIpRange().equals(getFromIpRange())
					&& rule.getToPortRange().equals(getToPortRange())
					&& rule.getToIpRange().equals(getToIpRange())
					&& rule.getProtocol().equals(getProtocol())
					&& rule.getDirection().equals(getDirection())
					&& rule.getAccess().equals(getAccess())
					&& (rule.getInterface().equals(Interface.ALL)
							|| getInterface().equals(Interface.ALL) || rule
							.getInterface().equals(getInterface()));
		}
		return false;
	}

	public Interface getInterface() {
		return moInterface;
	}

	public Interface setInterface(Interface inter) {
		if (inter == null) {
			inter = DEFAULT_INTERFACE;
		}
		Interface previous = getInterface();
		moInterface = inter;
		return previous;
	}

	public IpRange getFromIpRange() {
		return moFromIpRange;
	}

	public IpRange setFromIpRange(IpRange fromIpRange) {
		if (fromIpRange == null) {
			fromIpRange = DEFAULT_FROM_IP_RANGE;
		}
		IpRange previous = getFromIpRange();
		moFromIpRange = fromIpRange;
		return previous;
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

	public IpRange getToIpRange() {
		return moToIpRange;
	}

	public IpRange setToIpRange(IpRange toIpRange) {
		if (toIpRange == null) {
			toIpRange = DEFAULT_TO_IP_RANGE;
		}
		IpRange previous = getToIpRange();
		moToIpRange = toIpRange;
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

	public Protocol getProtocol() {
		return meProtocol;
	}

	public Protocol setProtocol(Protocol protocol) {
		if (protocol == null) {
			protocol = DEFAULT_PROTOCOL;
		}
		Protocol previous = getProtocol();
		this.meProtocol = protocol;
		return previous;
	}

	public Direction getDirection() {
		return meDirection;
	}

	public Direction setDirection(Direction direction) {
		if (direction == null) {
			direction = DEFAULT_DIRECTION;
		}
		Direction previous = getDirection();
		this.meDirection = direction;
		return previous;
	}

	public Access getAccess() {
		return meAccess;
	}

	public Access setAccess(Access access) {
		if (access == null) {
			access = DEFAULT_ACCESS;
		}
		Access previous = getAccess();
		this.meAccess = access;
		return previous;
	}

}

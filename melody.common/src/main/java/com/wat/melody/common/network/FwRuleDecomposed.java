package com.wat.melody.common.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRuleDecomposed {

	/*
	 * TODO : remove the device
	 */
	private Interface moInterface;
	private IpRange moFromIpRange;
	private PortRange moFromPortRange;
	private IpRange moToIpRange;
	private PortRange moToPortRange;
	private Protocol meProtocol;
	private Direction meDirection;
	private Access meAccess;

	public FwRuleDecomposed() {
		initInterface();
		initFromIpRange();
		initFromPortRange();
		initToIpRange();
		initToPortRange();
		initProtocol();
		initDirection();
		initAccess();
	}

	public FwRuleDecomposed(Interface inter, IpRange fromIpRange,
			PortRange fromPortRange, IpRange toIoRange, PortRange toPortRange,
			Protocol protocol, Direction direction, Access access) {
		setInterface(inter);
		setFromIpRange(fromIpRange);
		setFromPortRange(fromPortRange);
		setToIpRange(toIoRange);
		setToPortRange(toPortRange);
		setProtocol(protocol);
		setDirection(direction);
		setAccess(access);
	}

	public void initInterface() {
		moInterface = Interface.ALL;
	}

	public void initFromIpRange() {
		moFromIpRange = IpRange.ALL;
	}

	public void initFromPortRange() {
		moFromPortRange = PortRange.ALL;
	}

	public void initToIpRange() {
		moToIpRange = IpRange.ALL;
	}

	public void initToPortRange() {
		moToPortRange = PortRange.ALL;
	}

	public void initProtocol() {
		meProtocol = Protocol.TCP;
	}

	public void initDirection() {
		meDirection = Direction.IN;
	}

	public void initAccess() {
		meAccess = Access.DENY;
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interface.class.getCanonicalName()
					+ ".");
		}
		Interface previous = getInterface();
		moInterface = inter;
		return previous;
	}

	public IpRange getFromIpRange() {
		return moFromIpRange;
	}

	public IpRange setFromIpRange(IpRange ipRange) {
		if (ipRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + IpRange.class.getCanonicalName()
					+ ".");
		}
		IpRange previous = getFromIpRange();
		moFromIpRange = ipRange;
		return previous;
	}

	public PortRange getFromPortRange() {
		return moFromPortRange;
	}

	public PortRange setFromPortRange(PortRange fromPortRange) {
		if (fromPortRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRange.class.getCanonicalName()
					+ ".");
		}
		PortRange previous = getFromPortRange();
		moFromPortRange = fromPortRange;
		return previous;
	}

	public IpRange getToIpRange() {
		return moToIpRange;
	}

	public IpRange setToIpRange(IpRange ipRange) {
		if (ipRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + IpRange.class.getCanonicalName()
					+ ".");
		}
		IpRange previous = getToIpRange();
		moToIpRange = ipRange;
		return previous;
	}

	public PortRange getToPortRange() {
		return moToPortRange;
	}

	public PortRange setToPortRange(PortRange toPortRange) {
		if (toPortRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRange.class.getCanonicalName()
					+ ".");
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Protocol.class.getCanonicalName()
					+ ".");
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Direction.class.getCanonicalName()
					+ ".");
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Access.class.getCanonicalName()
					+ ".");
		}
		Access previous = getAccess();
		this.meAccess = access;
		return previous;
	}

}

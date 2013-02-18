package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalInterfaceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRuleDecomposed {

	private Interface moInterface;
	private IpRange moFromIpRange;
	private IpRange moToIpRange;
	private PortRange moPortRange;
	private Protocol meProtocol;
	private Access moAccess;

	public FwRuleDecomposed() {
		initInterface();
		initFromIpRange();
		initToIpRange();
		initPortRange();
		initProtocol();
		initAccess();
	}

	public FwRuleDecomposed(Interface inter, IpRange fromIpRange,
			IpRange toIoRange, PortRange portRange, Protocol protocol,
			Access access) {
		setInterface(inter);
		setFromIpRange(fromIpRange);
		setToIpRange(toIoRange);
		setPortRange(portRange);
		setProtocol(protocol);
		setAccess(access);
	}

	public void initInterface() {
		try {
			moInterface = Interface.parseString(Interface.ALL);
		} catch (IllegalInterfaceException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Interface with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initFromIpRange() {
		moFromIpRange = IpRange.ALL;
	}

	public void initToIpRange() {
		moToIpRange = IpRange.ALL;
	}

	public void initPortRange() {
		moPortRange = PortRange.ALL;
	}

	public void initProtocol() {
		meProtocol = Protocol.TCP;
	}

	public void initAccess() {
		moAccess = Access.DENY;
	}

	@Override
	public String toString() {
		return "{ " + "dev: " + getInterface() + ", from: " + getFromIpRange()
				+ ", to: " + getToIpRange() + ", port: " + getPortRange()
				+ ", protocol: " + getProtocol() + ", access: " + getAccess()
				+ " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof FwRuleDecomposed) {
			FwRuleDecomposed rule = (FwRuleDecomposed) anObject;
			return rule.getProtocol().equals(getProtocol())
					&& rule.getPortRange().equals(getPortRange())
					&& rule.getFromIpRange().equals(getFromIpRange())
					&& rule.getToIpRange().equals(getToIpRange())
					&& rule.getAccess().equals(getAccess())
					&& rule.getInterface().equals(getInterface());
		}
		return false;
	}

	public boolean equals(String fromIpRange, String toIpRange, int fromPort,
			int toPort, String protocol, String access) {
		return protocol.equalsIgnoreCase(getProtocol().getValue())
				&& fromPort == getPortRange().getFromPort().getValue()
				&& toPort == getPortRange().getToPort().getValue()
				&& fromIpRange.equals(getFromIpRange().getValue())
				&& toIpRange.equals(getToIpRange().getValue())
				&& access.equalsIgnoreCase(getAccess().getValue());
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

	public PortRange getPortRange() {
		return moPortRange;
	}

	public PortRange setPortRange(PortRange portRange) {
		if (portRange == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRange.class.getCanonicalName()
					+ ".");
		}
		PortRange previous = getPortRange();
		moPortRange = portRange;
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

	public Access getAccess() {
		return moAccess;
	}

	public Access setAccess(Access access) {
		if (access == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Access.class.getCanonicalName()
					+ ".");
		}
		Access previous = getAccess();
		this.moAccess = access;
		return previous;
	}

}

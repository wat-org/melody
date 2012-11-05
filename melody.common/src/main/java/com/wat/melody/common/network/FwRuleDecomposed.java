package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalInterfaceException;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRuleDecomposed {

	private Interface moFromInterface;
	private Interface moToInterface;
	private IpRange moFromIpRange;
	private IpRange moToIpRange;
	private PortRange moPortRange;
	private Protocol meProtocol;
	private Access moAccess;

	public FwRuleDecomposed() {
		initFromInterface();
		initToInterface();
		initFromIpRange();
		initToIpRange();
		initPortRange();
		initProtocol();
		initAccess();
	}

	public FwRuleDecomposed(Interface fromInterface, Interface toInterface,
			IpRange fromIpRange, IpRange toIoRange, PortRange portRange,
			Protocol protocol, Access access) {
		setFromInterface(fromInterface);
		setToInterface(toInterface);
		setFromIpRange(fromIpRange);
		setToIpRange(toIoRange);
		setPortRange(portRange);
		setProtocol(protocol);
		setAccess(access);
	}

	public void initFromInterface() {
		try {
			moFromInterface = Interface.parseString(Interface.ALL);
		} catch (IllegalInterfaceException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the FromInterface with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initToInterface() {
		try {
			moToInterface = Interface.parseString(Interface.ALL);
		} catch (IllegalInterfaceException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the ToInterface with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initFromIpRange() {
		try {
			moFromIpRange = IpRange.parseString(IpRange.ALL);
		} catch (IllegalIpRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the FromIpRange with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initToIpRange() {
		try {
			moToIpRange = IpRange.parseString(IpRange.ALL);
		} catch (IllegalIpRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the ToIpRange with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initPortRange() {
		try {
			moPortRange = PortRange.parseString(PortRange.ALL);
		} catch (IllegalPortRangeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the PortRange with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initProtocol() {
		meProtocol = Protocol.TCP;
	}

	public void initAccess() {
		moAccess = Access.DENY;
	}

	@Override
	public String toString() {
		return "{ " + "from: " + getFromIpRange() + ", to: " + getToIpRange()
				+ ", port: " + getPortRange() + ", protocol: " + getProtocol()
				+ ", access: " + getAccess() + " }";
	}

	public boolean equals(FwRuleDecomposed fw) {
		return fw.getProtocol().equals(getProtocol())
				&& fw.getPortRange().equals(getPortRange())
				&& fw.getFromIpRange().equals(getFromIpRange())
				&& fw.getToIpRange().equals(getToIpRange())
				&& fw.getAccess().equals(getAccess());
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

	public Interface getFromInterface() {
		return moFromInterface;
	}

	public Interface setFromInterface(Interface inter) {
		if (inter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Interface.");
		}
		Interface previous = getFromInterface();
		moFromInterface = inter;
		return previous;
	}

	public Interface getToInterface() {
		return moToInterface;
	}

	public Interface setToInterface(Interface inter) {
		if (inter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Interface.");
		}
		Interface previous = getToInterface();
		moToInterface = inter;
		return previous;
	}

	public IpRange getFromIpRange() {
		return moFromIpRange;
	}

	public IpRange setFromIpRange(IpRange ipRange) {
		IpRange previous = getFromIpRange();
		moFromIpRange = ipRange;
		return previous;
	}

	public IpRange getToIpRange() {
		return moToIpRange;
	}

	public IpRange setToIpRange(IpRange ipRange) {
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
					+ "Must be a valid PortRange.");
		}
		PortRange previous = getPortRange();
		moPortRange = portRange;
		return previous;
	}

	public Protocol getProtocol() {
		return meProtocol;
	}

	public Protocol setProtocol(Protocol protocol) {
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
					+ "Must be a valid Access.");
		}
		Access previous = getAccess();
		this.moAccess = access;
		return previous;
	}

}

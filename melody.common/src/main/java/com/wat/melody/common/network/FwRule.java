package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalInterfaceException;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.network.exception.IllegalProtocolsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRule {

	private Interface moFromInterface;
	private Interface moToInterface;
	private IpRanges moFromIpRanges;
	private IpRanges moToIpRanges;
	private PortRanges moPortRanges;
	private Protocols moProtocols;
	private Access moAccess;

	public FwRule() {
		initFromInterface();
		initToInterface();
		initFromIpRanges();
		initToIpRanges();
		initPortRanges();
		initProtocols();
		initAccess();
	}

	public FwRule(Interface fromInterface, Interface toInterface,
			IpRanges fromIpRanges, IpRanges toIoRanges, PortRanges portRanges,
			Protocols protocols, Access access) {
		setFromInterface(fromInterface);
		setToInterface(toInterface);
		setFromIpRanges(fromIpRanges);
		setToIpRanges(toIoRanges);
		setPortRanges(portRanges);
		setProtocols(protocols);
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

	public void initFromIpRanges() {
		try {
			moFromIpRanges = IpRanges.parseString(IpRange.ALL);
		} catch (IllegalIpRangesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the FromIpRanges with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initToIpRanges() {
		try {
			moToIpRanges = IpRanges.parseString(IpRange.ALL);
		} catch (IllegalIpRangesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the ToIpRanges with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initPortRanges() {
		try {
			moPortRanges = PortRanges.parseString(PortRange.ALL);
		} catch (IllegalPortRangesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the PortRanges with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initProtocols() {
		try {
			moProtocols = new Protocols(Protocol.TCP, Protocol.UDP);
		} catch (IllegalProtocolsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Protocols with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public void initAccess() {
		moAccess = Access.DENY;
	}

	@Override
	public String toString() {
		return "{ " + "from: " + getFromIpRanges() + ", to: " + getToIpRanges()
				+ ", ports: " + getPortRanges() + ", protocols: "
				+ getProtocols() + ", access: " + getAccess() + " }";
	}

	/**
	 * <p>
	 * Decompose this object into an equivalent collection of
	 * {@link FwRuleDecomposed} objects.
	 * </p>
	 * 
	 * <p>
	 * More formally, this object's 'from' {@link ipRanges}, 'to'
	 * {@link ipRanges}, and {@link Protocols} are decomposed into the
	 * corresponding {@link FwRuleDecomposed} objects, which contains 'from'
	 * {@link ipRange}, 'to' {@link ipRange}, and {@link Protocol}.
	 * </p>
	 * 
	 * @return an equivalent collection of {@link FwRuleDecomposed} objects.
	 */
	public FwRulesDecomposed decompose() {
		FwRulesDecomposed fws = new FwRulesDecomposed();
		Interface F = getFromInterface();
		Interface T = getToInterface();
		Access a = getAccess();
		for (Protocol p : getProtocols()) {
			for (PortRange r : getPortRanges()) {
				for (IpRange t : getToIpRanges()) {
					for (IpRange f : getFromIpRanges()) {
						fws.add(new FwRuleDecomposed(F, T, f, t, r, p, a));
					}
				}
			}
		}
		/*
		 * should call {@link FwRulesDecomposed#simplify()} here
		 */
		return fws;
	}

	public Interface getFromInterface() {
		return moFromInterface;
	}

	public Interface setFromInterface(Interface inter) {
		if (inter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interface.class.getCanonicalName()
					+ ".");
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
					+ "Must be a valid " + Interface.class.getCanonicalName()
					+ ".");
		}
		Interface previous = getToInterface();
		moToInterface = inter;
		return previous;
	}

	public IpRanges getFromIpRanges() {
		return moFromIpRanges;
	}

	public IpRanges setFromIpRanges(IpRanges ipRanges) {
		if (ipRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + IpRanges.class.getCanonicalName()
					+ ".");
		}
		IpRanges previous = getFromIpRanges();
		moFromIpRanges = ipRanges;
		return previous;
	}

	public IpRanges getToIpRanges() {
		return moToIpRanges;
	}

	public IpRanges setToIpRanges(IpRanges ipRanges) {
		if (ipRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + IpRanges.class.getCanonicalName()
					+ ".");
		}
		IpRanges previous = getToIpRanges();
		moToIpRanges = ipRanges;
		return previous;
	}

	public PortRanges getPortRanges() {
		return moPortRanges;
	}

	public PortRanges setPortRanges(PortRanges portRanges) {
		if (portRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRanges.class.getCanonicalName()
					+ ".");
		}
		PortRanges previous = getPortRanges();
		moPortRanges = portRanges;
		return previous;
	}

	public Protocols getProtocols() {
		return moProtocols;
	}

	public Protocols setProtocols(Protocols protocols) {
		if (protocols == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Protocols.class.getCanonicalName()
					+ ".");
		}
		Protocols previous = getProtocols();
		this.moProtocols = protocols;
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

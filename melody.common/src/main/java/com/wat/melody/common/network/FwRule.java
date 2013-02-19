package com.wat.melody.common.network;

import com.wat.melody.common.network.exception.IllegalProtocolsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRule {

	private Interfaces moInterfaces;
	private IpRanges moFromIpRanges;
	private PortRanges moFromPortRanges;
	private IpRanges moToIpRanges;
	private PortRanges moToPortRanges;
	private Protocols moProtocols;
	private Directions moDirections;
	private Access meAccess;

	public FwRule() {
		initInterfaces();
		initFromIpRanges();
		initFromPortRanges();
		initToIpRanges();
		initToPortRanges();
		initProtocols();
		initDirections();
		initAccess();
	}

	public FwRule(Interfaces interfaces, IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIoRanges,
			PortRanges toPortRanges, Protocols protocols,
			Directions directions, Access access) {
		setInterfaces(interfaces);
		setFromIpRanges(fromIpRanges);
		setFromPortRanges(fromPortRanges);
		setToIpRanges(toIoRanges);
		setToPortRanges(toPortRanges);
		setProtocols(protocols);
		setDirections(directions);
		setAccess(access);
	}

	public void initInterfaces() {
		moInterfaces = Interfaces.ALL;
	}

	public void initFromIpRanges() {
		moFromIpRanges = IpRanges.ALL;
	}

	public void initFromPortRanges() {
		moFromPortRanges = PortRanges.ALL;
	}

	public void initToIpRanges() {
		moToIpRanges = IpRanges.ALL;
	}

	public void initToPortRanges() {
		moToPortRanges = PortRanges.ALL;
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

	public void initDirections() {
		moDirections = Directions.ALL;
	}

	public void initAccess() {
		meAccess = Access.DENY;
	}

	@Override
	public String toString() {
		return "{ devives-name:" + getInterfaces() + ",from-ips: "
				+ getFromIpRanges() + ", from-ports: " + getFromPortRanges()
				+ ", to-ips: " + getToIpRanges() + ", to-ports: "
				+ getToPortRanges() + ", protocols: " + getProtocols()
				+ ", directions: " + getDirections() + ", access: "
				+ getAccess() + " }";
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
		Access a = getAccess();
		for (Interface i : getInterfaces()) {
			for (Direction d : getDirections()) {
				for (Protocol p : getProtocols()) {
					for (PortRange fp : getFromPortRanges()) {
						for (IpRange fi : getFromIpRanges()) {
							for (PortRange tp : getToPortRanges()) {
								for (IpRange ti : getToIpRanges()) {
									fws.add(new FwRuleDecomposed(i, fi, fp, ti,
											tp, p, d, a));
								}
							}
						}
					}
				}
			}
		}
		/*
		 * should call {@link FwRulesDecomposed#simplify()} here
		 */
		return fws;
	}

	public Interfaces getInterfaces() {
		return moInterfaces;
	}

	public Interfaces setInterfaces(Interfaces inter) {
		if (inter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interface.class.getCanonicalName()
					+ ".");
		}
		Interfaces previous = getInterfaces();
		moInterfaces = inter;
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

	public PortRanges getFromPortRanges() {
		return moFromPortRanges;
	}

	public PortRanges setFromPortRanges(PortRanges fromPortRanges) {
		if (fromPortRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRanges.class.getCanonicalName()
					+ ".");
		}
		PortRanges previous = getFromPortRanges();
		moFromPortRanges = fromPortRanges;
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

	public PortRanges getToPortRanges() {
		return moToPortRanges;
	}

	public PortRanges setToPortRanges(PortRanges toPortRanges) {
		if (toPortRanges == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PortRanges.class.getCanonicalName()
					+ ".");
		}
		PortRanges previous = getToPortRanges();
		moToPortRanges = toPortRanges;
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

	public Directions getDirections() {
		return moDirections;
	}

	public Directions setDirections(Directions directions) {
		if (directions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Directions.class.getCanonicalName()
					+ ".");
		}
		Directions previous = getDirections();
		this.moDirections = directions;
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

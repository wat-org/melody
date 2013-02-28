package com.wat.melody.common.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRule {

	private static Interfaces DEFAULT_INTERFACES = Interfaces.ALL;
	private static IpRanges DEFAULT_FROM_IP_RANGES = IpRanges.ALL;
	private static PortRanges DEFAULT_FROM_PORT_RANGES = PortRanges.ALL;
	private static IpRanges DEFAULT_TO_IP_RANGES = IpRanges.ALL;
	private static PortRanges DEFAULT_TO_PORT_RANGES = PortRanges.ALL;
	private static Protocols DEFAULT_PROTOCOLS = Protocols.ALL;
	private static Directions DEFAULT_DIRECTIONS = Directions.ALL;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private Interfaces moInterfaces = DEFAULT_INTERFACES;
	private IpRanges moFromIpRanges = DEFAULT_FROM_IP_RANGES;
	private PortRanges moFromPortRanges = DEFAULT_FROM_PORT_RANGES;
	private IpRanges moToIpRanges = DEFAULT_TO_IP_RANGES;
	private PortRanges moToPortRanges = DEFAULT_TO_PORT_RANGES;
	private Protocols moProtocols = DEFAULT_PROTOCOLS;
	private Directions moDirections = DEFAULT_DIRECTIONS;
	private Access meAccess = DEFAULT_ACCESS;

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

	@Override
	public int hashCode() {
		return getInterfaces().hashCode() + getFromIpRanges().hashCode()
				+ getFromPortRanges().hashCode() + getToIpRanges().hashCode();
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
		return fws;
	}

	public Interfaces getInterfaces() {
		return moInterfaces;
	}

	public Interfaces setInterfaces(Interfaces inter) {
		if (inter == null) {
			inter = DEFAULT_INTERFACES;
		}
		Interfaces previous = getInterfaces();
		moInterfaces = inter;
		return previous;
	}

	public IpRanges getFromIpRanges() {
		return moFromIpRanges;
	}

	public IpRanges setFromIpRanges(IpRanges fromIpRanges) {
		if (fromIpRanges == null) {
			fromIpRanges = DEFAULT_FROM_IP_RANGES;
		}
		IpRanges previous = getFromIpRanges();
		moFromIpRanges = fromIpRanges;
		return previous;
	}

	public PortRanges getFromPortRanges() {
		return moFromPortRanges;
	}

	public PortRanges setFromPortRanges(PortRanges fromPortRanges) {
		if (fromPortRanges == null) {
			fromPortRanges = DEFAULT_FROM_PORT_RANGES;
		}
		PortRanges previous = getFromPortRanges();
		moFromPortRanges = fromPortRanges;
		return previous;
	}

	public IpRanges getToIpRanges() {
		return moToIpRanges;
	}

	public IpRanges setToIpRanges(IpRanges toIpRanges) {
		if (toIpRanges == null) {
			toIpRanges = DEFAULT_TO_IP_RANGES;
		}
		IpRanges previous = getToIpRanges();
		moToIpRanges = toIpRanges;
		return previous;
	}

	public PortRanges getToPortRanges() {
		return moToPortRanges;
	}

	public PortRanges setToPortRanges(PortRanges toPortRanges) {
		if (toPortRanges == null) {
			toPortRanges = DEFAULT_TO_PORT_RANGES;
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
			protocols = DEFAULT_PROTOCOLS;
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
			directions = DEFAULT_DIRECTIONS;
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
			access = DEFAULT_ACCESS;
		}
		Access previous = getAccess();
		this.meAccess = access;
		return previous;
	}

}

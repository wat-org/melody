package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractFwRule implements FwRule {

	private static Interfaces DEFAULT_INTERFACES = Interfaces.ALL;
	private static IpRanges DEFAULT_FROM_IP_RANGES = IpRanges.ALL;
	private static IpRanges DEFAULT_TO_IP_RANGES = IpRanges.ALL;
	private static Directions DEFAULT_DIRECTIONS = Directions.ALL;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private Interfaces moInterfaces = DEFAULT_INTERFACES;
	private IpRanges moFromIpRanges = DEFAULT_FROM_IP_RANGES;
	private IpRanges moToIpRanges = DEFAULT_TO_IP_RANGES;
	private Directions moDirections = DEFAULT_DIRECTIONS;
	private Access meAccess = DEFAULT_ACCESS;

	public AbstractFwRule(Interfaces interfaces, IpRanges fromIpRanges,
			IpRanges toIpRanges, Directions directions, Access access) {
		setInterfaces(interfaces);
		setFromIpRanges(fromIpRanges);
		setToIpRanges(toIpRanges);
		setDirections(directions);
		setAccess(access);
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
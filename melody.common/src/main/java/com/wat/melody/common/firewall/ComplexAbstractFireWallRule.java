package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ComplexAbstractFireWallRule implements
		ComplexFireWallRule {

	private static IpRanges DEFAULT_FROM_IP_RANGES = IpRanges.ALL;
	private static IpRanges DEFAULT_TO_IP_RANGES = IpRanges.ALL;
	private static Directions DEFAULT_DIRECTIONS = Directions.ALL;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private IpRanges _fromIpRanges = DEFAULT_FROM_IP_RANGES;
	private IpRanges _toIpRanges = DEFAULT_TO_IP_RANGES;
	private Directions _directions = DEFAULT_DIRECTIONS;
	private Access _access = DEFAULT_ACCESS;

	public ComplexAbstractFireWallRule(IpRanges fromIpRanges,
			IpRanges toIpRanges, Directions directions, Access access) {
		setFromIpRanges(fromIpRanges);
		setToIpRanges(toIpRanges);
		setDirections(directions);
		setAccess(access);
	}

	public IpRanges getFromIpRanges() {
		return _fromIpRanges;
	}

	public IpRanges setFromIpRanges(IpRanges fromIpRanges) {
		if (fromIpRanges == null) {
			fromIpRanges = DEFAULT_FROM_IP_RANGES;
		}
		IpRanges previous = getFromIpRanges();
		_fromIpRanges = fromIpRanges;
		return previous;
	}

	public IpRanges getToIpRanges() {
		return _toIpRanges;
	}

	public IpRanges setToIpRanges(IpRanges toIpRanges) {
		if (toIpRanges == null) {
			toIpRanges = DEFAULT_TO_IP_RANGES;
		}
		IpRanges previous = getToIpRanges();
		_toIpRanges = toIpRanges;
		return previous;
	}

	public Directions getDirections() {
		return _directions;
	}

	public Directions setDirections(Directions directions) {
		if (directions == null) {
			directions = DEFAULT_DIRECTIONS;
		}
		Directions previous = getDirections();
		this._directions = directions;
		return previous;
	}

	public Access getAccess() {
		return _access;
	}

	public Access setAccess(Access access) {
		if (access == null) {
			access = DEFAULT_ACCESS;
		}
		Access previous = getAccess();
		this._access = access;
		return previous;
	}

}
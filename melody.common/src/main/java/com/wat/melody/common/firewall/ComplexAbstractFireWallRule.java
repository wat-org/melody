package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.IpRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ComplexAbstractFireWallRule implements
		ComplexFireWallRule {

	private static Addresses DEFAULT_FROM_ADDRESSES = new Addresses(IpRange.ALL);
	private static Addresses DEFAULT_TO_ADDRESSES = new Addresses(IpRange.ALL);
	private static Directions DEFAULT_DIRECTIONS = Directions.ALL;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private Addresses _fromAddresses = DEFAULT_FROM_ADDRESSES;
	private Addresses _toAddresses = DEFAULT_TO_ADDRESSES;
	private Directions _directions = DEFAULT_DIRECTIONS;
	private Access _access = DEFAULT_ACCESS;

	public ComplexAbstractFireWallRule(Addresses fromAddresses,
			Addresses toAddresses, Directions directions, Access access) {
		setFromAddresses(fromAddresses);
		setToAddresses(toAddresses);
		setDirections(directions);
		setAccess(access);
	}

	public Addresses getFromAddresses() {
		return _fromAddresses;
	}

	public Addresses setFromAddresses(Addresses fromAddresses) {
		if (fromAddresses == null) {
			fromAddresses = DEFAULT_FROM_ADDRESSES;
		}
		Addresses previous = getFromAddresses();
		_fromAddresses = fromAddresses;
		return previous;
	}

	public Addresses getToAddresses() {
		return _toAddresses;
	}

	public Addresses setToAddresses(Addresses toAddresses) {
		if (toAddresses == null) {
			toAddresses = DEFAULT_TO_ADDRESSES;
		}
		Addresses previous = getToAddresses();
		_toAddresses = toAddresses;
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
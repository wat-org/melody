package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.IpRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SimpleAbstractFireWallRule implements SimpleFireWallRule {

	private static IpRange DEFAULT_FROM_ADDRESS = IpRange.ALL;
	private static IpRange DEFAULT_TO_ADDRESS = IpRange.ALL;
	private static Direction DEFAULT_DIRECTION = Direction.IN;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private Address _fromAddress = DEFAULT_FROM_ADDRESS;
	private Address _toAddress = DEFAULT_TO_ADDRESS;
	private Direction _direction = DEFAULT_DIRECTION;
	private Access _access = DEFAULT_ACCESS;

	public SimpleAbstractFireWallRule(Address fromAddress, Address toAddress,
			Direction direction, Access access) {
		setFromAddress(fromAddress);
		setToAddress(toAddress);
		setDirection(direction);
		setAccess(access);
	}

	@Override
	public Address getFromAddress() {
		return _fromAddress;
	}

	public Address setFromAddress(Address fromAddress) {
		if (fromAddress == null) {
			fromAddress = DEFAULT_FROM_ADDRESS;
		}
		Address previous = getFromAddress();
		_fromAddress = fromAddress;
		return previous;
	}

	@Override
	public Address getToAddress() {
		return _toAddress;
	}

	public Address setToAddress(Address toAddress) {
		if (toAddress == null) {
			toAddress = DEFAULT_TO_ADDRESS;
		}
		Address previous = getToAddress();
		_toAddress = toAddress;
		return previous;
	}

	public Direction getDirection() {
		return _direction;
	}

	public Direction setDirection(Direction direction) {
		if (direction == null) {
			direction = DEFAULT_DIRECTION;
		}
		Direction previous = getDirection();
		this._direction = direction;
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
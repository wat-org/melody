package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SimpleAbstractFireWallRule implements SimpleFireWallRule {

	private static IpRange DEFAULT_FROM_IP_RANGE = IpRange.ALL;
	private static IpRange DEFAULT_TO_IP_RANGE = IpRange.ALL;
	private static Direction DEFAULT_DIRECTION = Direction.IN;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private IpRange _fromIpRange = DEFAULT_FROM_IP_RANGE;
	private IpRange _toIpRange = DEFAULT_TO_IP_RANGE;
	private Direction _direction = DEFAULT_DIRECTION;
	private Access _access = DEFAULT_ACCESS;

	public SimpleAbstractFireWallRule(IpRange fromIpRange, IpRange toIpRange,
			Direction direction, Access access) {
		setFromIpRange(fromIpRange);
		setToIpRange(toIpRange);
		setDirection(direction);
		setAccess(access);
	}

	public IpRange getFromIpRange() {
		return _fromIpRange;
	}

	public IpRange setFromIpRange(IpRange fromIpRange) {
		if (fromIpRange == null) {
			fromIpRange = DEFAULT_FROM_IP_RANGE;
		}
		IpRange previous = getFromIpRange();
		_fromIpRange = fromIpRange;
		return previous;
	}

	public IpRange getToIpRange() {
		return _toIpRange;
	}

	public IpRange setToIpRange(IpRange toIpRange) {
		if (toIpRange == null) {
			toIpRange = DEFAULT_TO_IP_RANGE;
		}
		IpRange previous = getToIpRange();
		_toIpRange = toIpRange;
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
package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractFwRuleDecomposed implements FwRuleDecomposed {

	private static Interface DEFAULT_INTERFACE = Interface.ALL;
	private static IpRange DEFAULT_FROM_IP_RANGE = IpRange.ALL;
	private static IpRange DEFAULT_TO_IP_RANGE = IpRange.ALL;
	private static Direction DEFAULT_DIRECTION = Direction.IN;
	private static Access DEFAULT_ACCESS = Access.DENY;

	private Interface moInterface = DEFAULT_INTERFACE;
	private IpRange moFromIpRange = DEFAULT_FROM_IP_RANGE;
	private IpRange moToIpRange = DEFAULT_TO_IP_RANGE;
	private Direction meDirection = DEFAULT_DIRECTION;
	private Access meAccess = DEFAULT_ACCESS;

	public AbstractFwRuleDecomposed(Interface inter, IpRange fromIpRange,
			IpRange toIpRange, Direction direction, Access access) {
		setInterface(inter);
		setFromIpRange(fromIpRange);
		setToIpRange(toIpRange);
		setDirection(direction);
		setAccess(access);
	}

	public Interface getInterface() {
		return moInterface;
	}

	public Interface setInterface(Interface inter) {
		if (inter == null) {
			inter = DEFAULT_INTERFACE;
		}
		Interface previous = getInterface();
		moInterface = inter;
		return previous;
	}

	public IpRange getFromIpRange() {
		return moFromIpRange;
	}

	public IpRange setFromIpRange(IpRange fromIpRange) {
		if (fromIpRange == null) {
			fromIpRange = DEFAULT_FROM_IP_RANGE;
		}
		IpRange previous = getFromIpRange();
		moFromIpRange = fromIpRange;
		return previous;
	}

	public IpRange getToIpRange() {
		return moToIpRange;
	}

	public IpRange setToIpRange(IpRange toIpRange) {
		if (toIpRange == null) {
			toIpRange = DEFAULT_TO_IP_RANGE;
		}
		IpRange previous = getToIpRange();
		moToIpRange = toIpRange;
		return previous;
	}

	public Direction getDirection() {
		return meDirection;
	}

	public Direction setDirection(Direction direction) {
		if (direction == null) {
			direction = DEFAULT_DIRECTION;
		}
		Direction previous = getDirection();
		this.meDirection = direction;
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
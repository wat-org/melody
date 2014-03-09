package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleIcmpFireWallRule extends SimpleAbstractFireWallRule {

	private static IcmpType DEFAULT_TYPE = IcmpType.ALL;
	private static IcmpCode DEFAULT_CODE = IcmpCode.ALL;

	private IcmpType _type = DEFAULT_TYPE;
	private IcmpCode _code = DEFAULT_CODE;

	public SimpleIcmpFireWallRule(Address fromAddress, Address toAddress,
			IcmpType type, IcmpCode code, Direction direction, Access access) {
		super(fromAddress, toAddress, direction, access);
		setType(type);
		setCode(code);
	}

	@Override
	public int hashCode() {
		return getFromAddress().hashCode() + getToAddress().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol: ");
		str.append(getProtocol());
		str.append(", from-ips: ");
		str.append(getFromAddress());
		str.append(", to-ips: ");
		str.append(getToAddress());
		str.append(", types: ");
		str.append(getType());
		str.append(", codes: ");
		str.append(getCode());
		str.append(", directions: ");
		str.append(getDirection());
		str.append(", access: ");
		str.append(getAccess());
		str.append(" }");
		return str.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof SimpleIcmpFireWallRule) {
			SimpleIcmpFireWallRule rule = (SimpleIcmpFireWallRule) anObject;
			return rule.getFromAddress().equals(getFromAddress())
					&& rule.getToAddress().equals(getToAddress())
					&& rule.getType().equals(getType())
					&& rule.getCode().equals(getCode())
					&& rule.getDirection().equals(getDirection())
					&& rule.getAccess().equals(getAccess());
		}
		return false;
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.ICMP;
	}

	public IcmpType getType() {
		return _type;
	}

	public IcmpType setType(IcmpType fromPortRange) {
		if (fromPortRange == null) {
			fromPortRange = DEFAULT_TYPE;
		}
		IcmpType previous = getType();
		_type = fromPortRange;
		return previous;
	}

	public IcmpCode getCode() {
		return _code;
	}

	public IcmpCode setCode(IcmpCode toPortRange) {
		if (toPortRange == null) {
			toPortRange = DEFAULT_CODE;
		}
		IcmpCode previous = getCode();
		_code = toPortRange;
		return previous;
	}

}
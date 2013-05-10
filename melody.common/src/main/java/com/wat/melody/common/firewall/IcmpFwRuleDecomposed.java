package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpFwRuleDecomposed extends AbstractFwRuleDecomposed {

	private static IcmpType DEFAULT_TYPE = IcmpType.ALL;
	private static IcmpCode DEFAULT_CODE = IcmpCode.ALL;

	private IcmpType _type = DEFAULT_TYPE;
	private IcmpCode _code = DEFAULT_CODE;

	public IcmpFwRuleDecomposed(Interface inter, IpRange fromIpRange,
			IpRange toIpRange, IcmpType type, IcmpCode code,
			Direction direction, Access access) {
		super(inter, fromIpRange, toIpRange, direction, access);
		setType(type);
		setCode(code);
	}

	@Override
	public int hashCode() {
		return getFromIpRange().hashCode() + getToIpRange().hashCode();
	}

	@Override
	public String toString() {
		return "{ " + "devive-name: " + getInterface() + ", protocol: "
				+ getProtocol() + ", from-ip: " + getFromIpRange()
				+ ", to-ip: " + getToIpRange() + ", type: " + getType()
				+ ", code: " + getCode() + ", direction: " + getDirection()
				+ ", access: " + getAccess() + " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof IcmpFwRuleDecomposed) {
			IcmpFwRuleDecomposed rule = (IcmpFwRuleDecomposed) anObject;
			return rule.getFromIpRange().equals(getFromIpRange())
					&& rule.getToIpRange().equals(getToIpRange())
					&& rule.getType().equals(getType())
					&& rule.getCode().equals(getCode())
					&& rule.getDirection().equals(getDirection())
					&& rule.getAccess().equals(getAccess())
					&& (rule.getInterface().equals(Interface.ALL)
							|| getInterface().equals(Interface.ALL) || rule
							.getInterface().equals(getInterface()));
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
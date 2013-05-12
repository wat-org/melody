package com.wat.melody.common.firewall;

import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ComplexIcmpFireWallRule extends ComplexAbstractFireWallRule {

	private static IcmpTypes DEFAULT_TYPES = IcmpTypes.ALL;
	private static IcmpCodes DEFAULT_CODES = IcmpCodes.ALL;

	private IcmpTypes _types = DEFAULT_TYPES;
	private IcmpCodes _codes = DEFAULT_CODES;

	public ComplexIcmpFireWallRule(IpRanges fromIpRanges, IpRanges toIpRanges,
			IcmpTypes types, IcmpCodes codes, Directions directions,
			Access access) {
		super(fromIpRanges, toIpRanges, directions, access);
		setTypes(types);
		setCodes(codes);
	}

	@Override
	public int hashCode() {
		return getFromIpRanges().hashCode() + getToIpRanges().hashCode();
	}

	@Override
	public String toString() {
		return "{ " + "protocol: " + getProtocol() + ",from-ips: "
				+ getFromIpRanges() + ", to-ips: " + getToIpRanges()
				+ ", types: " + getTypes() + ", codes: " + getCodes()
				+ ", directions: " + getDirections() + ", access: "
				+ getAccess() + " }";
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.ICMP;
	}

	/**
	 * <p>
	 * Decompose this object into an equivalent collection of
	 * {@link SimpleFireWallRule} objects.
	 * </p>
	 * 
	 * <p>
	 * More formally, this object's 'from' {@link IpRanges}, 'from'
	 * {@link PortRanges}, 'to' {@link IpRanges}, 'to' {@link PortRanges} and
	 * {@link Directions} are decomposed into the corresponding
	 * {@link SimpleFireWallRule} objects, which contains equivalent 'from'
	 * {@link IpRange}, 'from' {@link PortRange}, 'to' {@link IpRange}, 'to'
	 * {@link PortRange} and {@link Direction}.
	 * </p>
	 * 
	 * @return an equivalent collection of {@link SimpleFireWallRule} objects.
	 */
	public FireWallRules decompose() {
		FireWallRules fws = new FireWallRules();
		Access a = getAccess();
		for (Direction d : getDirections()) {
			for (IpRange fi : getFromIpRanges()) {
				for (IpRange ti : getToIpRanges()) {
					for (IcmpType t : getTypes()) {
						for (IcmpCode c : getCodes()) {
							fws.add(new SimpleIcmpFireWallRule(fi, ti, t, c, d,
									a));
						}
					}
				}
			}
		}
		return fws;
	}

	public IcmpTypes getTypes() {
		return _types;
	}

	public IcmpTypes setTypes(IcmpTypes fromPortRange) {
		if (fromPortRange == null) {
			fromPortRange = DEFAULT_TYPES;
		}
		IcmpTypes previous = getTypes();
		_types = fromPortRange;
		return previous;
	}

	public IcmpCodes getCodes() {
		return _codes;
	}

	public IcmpCodes setCodes(IcmpCodes toPortRange) {
		if (toPortRange == null) {
			toPortRange = DEFAULT_CODES;
		}
		IcmpCodes previous = getCodes();
		_codes = toPortRange;
		return previous;
	}

}
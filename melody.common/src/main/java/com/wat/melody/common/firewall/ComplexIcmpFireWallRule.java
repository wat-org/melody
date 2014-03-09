package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.Addresses;

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

	public ComplexIcmpFireWallRule(Addresses fromAddresses,
			Addresses toAddresses, IcmpTypes types, IcmpCodes codes,
			Directions directions, Access access) {
		super(fromAddresses, toAddresses, directions, access);
		setTypes(types);
		setCodes(codes);
	}

	@Override
	public int hashCode() {
		return getFromAddresses().hashCode() + getToAddresses().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol: ");
		str.append(getProtocol());
		str.append(", from-ips: ");
		str.append(getFromAddresses());
		str.append(", to-ips: ");
		str.append(getToAddresses());
		str.append(", types: ");
		str.append(getTypes());
		str.append(", codes: ");
		str.append(getCodes());
		str.append(", directions: ");
		str.append(getDirections());
		str.append(", access: ");
		str.append(getAccess());
		str.append(" }");
		return str.toString();
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
	 * More formally, this object's 'from' {@link Addresses}, 'to'
	 * {@link Addresses}, {@link IcmpTypes}, {@link IcmpCodes} and
	 * {@link Directions} are decomposed into the corresponding
	 * {@link SimpleFireWallRule} objects, which contains equivalent 'from'
	 * {@link Address}, 'to' {@link Address}, {@link IcmpType}, {@link IcmpCode}
	 * and {@link Direction}.
	 * </p>
	 * 
	 * @return an equivalent collection of {@link SimpleFireWallRule} objects.
	 */
	public FireWallRules decompose() {
		FireWallRules fws = new FireWallRules();
		Access a = getAccess();
		for (Direction d : getDirections()) {
			for (Address fi : getFromAddresses()) {
				for (Address ti : getToAddresses()) {
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
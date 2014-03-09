package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.PortRanges;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ComplexAbstractTcpUdpFireWallRule extends
		ComplexAbstractFireWallRule {

	private static PortRanges DEFAULT_FROM_PORT_RANGES = PortRanges.ALL;
	private static PortRanges DEFAULT_TO_PORT_RANGES = PortRanges.ALL;

	private PortRanges _fromPortRanges = DEFAULT_FROM_PORT_RANGES;
	private PortRanges _toPortRanges = DEFAULT_TO_PORT_RANGES;

	public ComplexAbstractTcpUdpFireWallRule(Addresses fromAddresses,
			PortRanges fromPortRanges, Addresses toAddresses,
			PortRanges toPortRanges, Directions directions, Access access) {
		super(fromAddresses, toAddresses, directions, access);
		setFromPortRanges(fromPortRanges);
		setToPortRanges(toPortRanges);
	}

	public abstract SimpleFireWallRule newSimpleFireWallRule(
			Address fromAddress, PortRange fromPortRange, Address toAddress,
			PortRange toPortRange, Direction direction, Access access);

	@Override
	public int hashCode() {
		return getFromAddresses().hashCode() + getFromPortRanges().hashCode()
				+ getToAddresses().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol: ");
		str.append(getProtocol());
		str.append(", from-ips: ");
		str.append(getFromAddresses());
		str.append(", from-ports: ");
		str.append(getFromPortRanges());
		str.append(", to-ips: ");
		str.append(getToAddresses());
		str.append(", to-ports: ");
		str.append(getToPortRanges());
		str.append(", directions: ");
		str.append(getDirections());
		str.append(", access: ");
		str.append(getAccess());
		str.append(" }");
		return str.toString();
	}

	/**
	 * <p>
	 * Decompose this object into an equivalent collection of
	 * {@link SimpleFireWallRule} objects.
	 * </p>
	 * 
	 * <p>
	 * More formally, this object's 'from' {@link Addresses}, 'from'
	 * {@link PortRanges}, 'to' {@link Addresses}, 'to' {@link PortRanges} and
	 * {@link Directions} are decomposed into the corresponding
	 * {@link SimpleFireWallRule} objects, which contains equivalent 'from'
	 * {@link Address}, 'from' {@link PortRange}, 'to' {@link Address}, 'to'
	 * {@link PortRange} and {@link Direction}.
	 * </p>
	 * 
	 * @return an equivalent collection of {@link SimpleFireWallRule} objects.
	 */
	public FireWallRules decompose() {
		FireWallRules fws = new FireWallRules();
		Access a = getAccess();
		for (Direction d : getDirections()) {
			for (PortRange fp : getFromPortRanges()) {
				for (Address fi : getFromAddresses()) {
					for (PortRange tp : getToPortRanges()) {
						for (Address ti : getToAddresses()) {
							fws.add(newSimpleFireWallRule(fi, fp, ti, tp, d, a));
						}
					}
				}
			}
		}
		return fws;
	}

	public PortRanges getFromPortRanges() {
		return _fromPortRanges;
	}

	public PortRanges setFromPortRanges(PortRanges fromPortRanges) {
		if (fromPortRanges == null) {
			fromPortRanges = DEFAULT_FROM_PORT_RANGES;
		}
		PortRanges previous = getFromPortRanges();
		_fromPortRanges = fromPortRanges;
		return previous;
	}

	public PortRanges getToPortRanges() {
		return _toPortRanges;
	}

	public PortRanges setToPortRanges(PortRanges toPortRanges) {
		if (toPortRanges == null) {
			toPortRanges = DEFAULT_TO_PORT_RANGES;
		}
		PortRanges previous = getToPortRanges();
		_toPortRanges = toPortRanges;
		return previous;
	}

}
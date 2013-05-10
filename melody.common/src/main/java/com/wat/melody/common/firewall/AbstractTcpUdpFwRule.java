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
public abstract class AbstractTcpUdpFwRule extends AbstractFwRule {

	private static PortRanges DEFAULT_FROM_PORT_RANGES = PortRanges.ALL;
	private static PortRanges DEFAULT_TO_PORT_RANGES = PortRanges.ALL;

	private PortRanges moFromPortRanges = DEFAULT_FROM_PORT_RANGES;
	private PortRanges moToPortRanges = DEFAULT_TO_PORT_RANGES;

	public AbstractTcpUdpFwRule(Interfaces interfaces, IpRanges fromIpRanges,
			PortRanges fromPortRanges, IpRanges toIpRanges,
			PortRanges toPortRanges, Directions directions, Access access) {
		super(interfaces, fromIpRanges, toIpRanges, directions, access);
		setFromPortRanges(fromPortRanges);
		setToPortRanges(toPortRanges);
	}

	public abstract FwRuleDecomposed newFwRuleDecomposed(Interface inter,
			IpRange fromIpRange, PortRange fromPortRange, IpRange toIpRange,
			PortRange toPortRange, Direction direction, Access access);

	@Override
	public int hashCode() {
		return getInterfaces().hashCode() + getFromIpRanges().hashCode()
				+ getFromPortRanges().hashCode() + getToIpRanges().hashCode();
	}

	@Override
	public String toString() {
		return "{ devives-name:" + getInterfaces() + ", protocol: "
				+ getProtocol() + ",from-ips: " + getFromIpRanges()
				+ ", from-ports: " + getFromPortRanges() + ", to-ips: "
				+ getToIpRanges() + ", to-ports: " + getToPortRanges()
				+ ", directions: " + getDirections() + ", access: "
				+ getAccess() + " }";
	}

	/**
	 * <p>
	 * Decompose this object into an equivalent collection of
	 * {@link FwRuleDecomposed} objects.
	 * </p>
	 * 
	 * <p>
	 * More formally, this object's 'from' {@link IpRanges}, 'from'
	 * {@link PortRanges}, 'to' {@link IpRanges}, 'to' {@link PortRanges} and
	 * {@link Directions} are decomposed into the corresponding
	 * {@link FwRuleDecomposed} objects, which contains equivalent 'from'
	 * {@link IpRange}, 'from' {@link PortRange}, 'to' {@link IpRange}, 'to'
	 * {@link PortRange} and {@link Direction}.
	 * </p>
	 * 
	 * @return an equivalent collection of {@link FwRuleDecomposed} objects.
	 */
	public FwRulesDecomposed decompose() {
		FwRulesDecomposed fws = new FwRulesDecomposed();
		Access a = getAccess();
		for (Interface i : getInterfaces()) {
			for (Direction d : getDirections()) {
				for (PortRange fp : getFromPortRanges()) {
					for (IpRange fi : getFromIpRanges()) {
						for (PortRange tp : getToPortRanges()) {
							for (IpRange ti : getToIpRanges()) {
								fws.add(newFwRuleDecomposed(i, fi, fp, ti, tp,
										d, a));
							}
						}
					}
				}
			}
		}
		return fws;
	}

	public PortRanges getFromPortRanges() {
		return moFromPortRanges;
	}

	public PortRanges setFromPortRanges(PortRanges fromPortRanges) {
		if (fromPortRanges == null) {
			fromPortRanges = DEFAULT_FROM_PORT_RANGES;
		}
		PortRanges previous = getFromPortRanges();
		moFromPortRanges = fromPortRanges;
		return previous;
	}

	public PortRanges getToPortRanges() {
		return moToPortRanges;
	}

	public PortRanges setToPortRanges(PortRanges toPortRanges) {
		if (toPortRanges == null) {
			toPortRanges = DEFAULT_TO_PORT_RANGES;
		}
		PortRanges previous = getToPortRanges();
		moToPortRanges = toPortRanges;
		return previous;
	}

}
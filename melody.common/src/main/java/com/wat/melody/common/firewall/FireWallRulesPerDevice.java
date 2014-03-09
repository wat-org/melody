package com.wat.melody.common.firewall;

import java.util.HashMap;

import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * Associates {@link FireWallRules} to {@link NetworkDeviceNameRef}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesPerDevice extends
		HashMap<NetworkDeviceNameRef, FireWallRules> {

	private static final long serialVersionUID = 2206075263169599238L;

	public FireWallRulesPerDevice() {
		super();
	}

	public FireWallRulesPerDevice(FireWallRulesPerDevice rulesPerDevice) {
		super(rulesPerDevice);
	}

	public boolean contains(NetworkDeviceNameRef ref, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(ref)) {
			return false;
		}
		return get(ref).contains(rule);
	}

	public boolean contains(NetworkDeviceNameRef ref, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(ref)) {
			return false;
		}
		return get(ref).containsAll(rule.decompose());
	}

	public boolean merge(NetworkDeviceNameRef ref, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(ref)) {
			put(ref, new FireWallRules());
		}
		return get(ref).add(rule);
	}

	public boolean merge(NetworkDeviceNameRef ref, FireWallRules rules) {
		if (rules == null) {
			return false;
		}
		if (!containsKey(ref)) {
			put(ref, new FireWallRules());
		}
		return get(ref).addAll(rules);
	}

	public boolean merge(NetworkDeviceNameRef ref, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		return merge(ref, rule.decompose());
	}

	public boolean merge(NetworkDeviceNameRefs refs, SimpleFireWallRule rule) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= merge(ref, rule);
		}
		return changed;
	}

	public boolean merge(NetworkDeviceNameRefs refs, FireWallRules rules) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= merge(ref, rules);
		}
		return changed;
	}

	public boolean merge(NetworkDeviceNameRefs refs, ComplexFireWallRule rule) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		if (rule == null) {
			return false;
		}
		// doing so, .decompose() is just call one time
		FireWallRules decomposed = rule.decompose();
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= merge(ref, decomposed);
		}
		return changed;
	}

	public void merge(FireWallRulesPerDevice rulesPerDevice) {
		for (NetworkDeviceNameRef ref : rulesPerDevice.keySet()) {
			merge(ref, rulesPerDevice.get(ref));
		}
	}

	public boolean remove(NetworkDeviceNameRef ref, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(ref)) {
			return false;
		}
		return get(ref).remove(rule);
	}

	public boolean remove(NetworkDeviceNameRef ref, FireWallRules rules) {
		if (rules == null) {
			return false;
		}
		if (!containsKey(ref)) {
			return false;
		}
		return get(ref).removeAll(rules);
	}

	public boolean remove(NetworkDeviceNameRef ref, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		return remove(ref, rule.decompose());
	}

	public boolean remove(NetworkDeviceNameRefs refs, SimpleFireWallRule rule) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= remove(ref, rule);
		}
		return changed;
	}

	public boolean remove(NetworkDeviceNameRefs refs, FireWallRules rules) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= remove(ref, rules);
		}
		return changed;
	}

	public boolean remove(NetworkDeviceNameRefs refs, ComplexFireWallRule rule) {
		if (refs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceNameRefs.class.getCanonicalName() + ".");
		}
		if (rule == null) {
			return false;
		}
		// doing so, .decompose() is just call one time
		FireWallRules decomposed = rule.decompose();
		boolean changed = false;
		for (NetworkDeviceNameRef ref : refs) {
			changed |= remove(ref, decomposed);
		}
		return changed;
	}

	public void remove(FireWallRulesPerDevice rulesPerDevice) {
		for (NetworkDeviceNameRef ref : rulesPerDevice.keySet()) {
			remove(ref, rulesPerDevice.get(ref));
		}
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("");
		for (NetworkDeviceNameRef ref : keySet()) {
			FireWallRules rules = get(ref);
			if (rules == null || rules.size() == 0) {
				continue;
			}
			str.append(SysTool.NEW_LINE + "device-name:" + ref);
			str.append(rules.toString().replaceAll(SysTool.NEW_LINE,
					SysTool.NEW_LINE + "  "));
		}
		return str.length() == 0 ? SysTool.NEW_LINE + "no firewall rules" : str
				.toString();
	}

	/**
	 * @param netdev
	 *            is the name of the network interface to query.
	 * 
	 * @return the {@link FireWallRules} associated to the given
	 *         {@link NetworkDeviceName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given network interface name is <tt>null</tt>.
	 * 
	 * @see #getFireWallRules()
	 */
	public FireWallRules getFireWallRules(NetworkDeviceName netdev) {
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		FireWallRules rules = new FireWallRules();
		FireWallRules tmp = get(NetworkDeviceNameRef.ALL);
		if (tmp != null) {
			rules.addAll(tmp);
		}
		tmp = get(NetworkDeviceNameRef.fromNetworkDeviceName(netdev));
		if (tmp != null) {
			rules.addAll(tmp);
		}
		return rules;
	}

	/**
	 * @return the {@link FireWallRules} associated to the network device name
	 *         'all'.
	 * 
	 * @see #getFireWallRules(NetworkDeviceName)
	 */
	public FireWallRules getFireWallRules() {
		FireWallRules tmp = get(NetworkDeviceNameRef.ALL);
		if (tmp == null) {
			tmp = new FireWallRules();
		}
		return tmp;
	}

}
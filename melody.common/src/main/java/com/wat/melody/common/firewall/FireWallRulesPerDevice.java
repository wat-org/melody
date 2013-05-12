package com.wat.melody.common.firewall;

import java.util.HashMap;

import com.wat.melody.common.ex.Util;

/**
 * <p>
 * Associates {@link FireWallRules} to {@link Interface}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesPerDevice extends HashMap<Interface, FireWallRules> {

	private static final long serialVersionUID = 2206075263169599238L;

	/*
	 * TODO : try to use a NetworkDeviceName instead of an Interface, and use
	 * the null key for "all" devices
	 */

	public FireWallRulesPerDevice() {
		super();
	}

	public FireWallRulesPerDevice(FireWallRulesPerDevice rulesPerDevice) {
		super(rulesPerDevice);
	}

	public boolean contains(Interface inter, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(inter)) {
			return false;
		}
		return get(inter).contains(rule);
	}

	public boolean contains(Interface inter, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(inter)) {
			return false;
		}
		return get(inter).containsAll(rule.decompose());
	}

	public boolean merge(Interface inter, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(inter)) {
			put(inter, new FireWallRules());
		}
		return get(inter).add(rule);
	}

	public boolean merge(Interface inter, FireWallRules rules) {
		if (rules == null) {
			return false;
		}
		if (!containsKey(inter)) {
			put(inter, new FireWallRules());
		}
		return get(inter).addAll(rules);
	}

	public boolean merge(Interface inter, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		return merge(inter, rule.decompose());
	}

	public boolean merge(Interfaces inters, SimpleFireWallRule rule) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= merge(inter, rule);
		}
		return changed;
	}

	public boolean merge(Interfaces inters, FireWallRules rules) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= merge(inter, rules);
		}
		return changed;
	}

	public boolean merge(Interfaces inters, ComplexFireWallRule rule) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		if (rule == null) {
			return false;
		}
		// doing so, .decompose() is just call one time
		FireWallRules decomposed = rule.decompose();
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= merge(inter, decomposed);
		}
		return changed;
	}

	public void merge(FireWallRulesPerDevice rulesPerDevice) {
		for (Interface inter : rulesPerDevice.keySet()) {
			merge(inter, rulesPerDevice.get(inter));
		}
	}

	public boolean remove(Interface inter, SimpleFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		if (!containsKey(inter)) {
			return false;
		}
		return get(inter).remove(rule);
	}

	public boolean remove(Interface inter, FireWallRules rules) {
		if (rules == null) {
			return false;
		}
		if (!containsKey(inter)) {
			return false;
		}
		return get(inter).removeAll(rules);
	}

	public boolean remove(Interface inter, ComplexFireWallRule rule) {
		if (rule == null) {
			return false;
		}
		return remove(inter, rule.decompose());
	}

	public boolean remove(Interfaces inters, SimpleFireWallRule rule) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= remove(inter, rule);
		}
		return changed;
	}

	public boolean remove(Interfaces inters, FireWallRules rules) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= remove(inter, rules);
		}
		return changed;
	}

	public boolean remove(Interfaces inters, ComplexFireWallRule rule) {
		if (inters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Interfaces.class.getCanonicalName()
					+ ".");
		}
		if (rule == null) {
			return false;
		}
		// doing so, .decompose() is just call one time
		FireWallRules decomposed = rule.decompose();
		boolean changed = false;
		for (Interface inter : inters) {
			changed |= remove(inter, decomposed);
		}
		return changed;
	}

	public void remove(FireWallRulesPerDevice rulesPerDevice) {
		for (Interface inter : rulesPerDevice.keySet()) {
			remove(inter, rulesPerDevice.get(inter));
		}
	}

	@Override
	public String toString() {
		String res = "";
		for (Interface inter : keySet()) {
			FireWallRules rules = get(inter);
			if (rules == null || rules.size() == 0) {
				continue;
			}
			res += Util.NEW_LINE
					+ "device-name:"
					+ inter
					+ rules.toString().replaceAll(Util.NEW_LINE,
							Util.NEW_LINE + "  ");
		}
		return res.length() == 0 ? Util.NEW_LINE + "no firewall rules" : res;
	}

}
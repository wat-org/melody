package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * Contains a set of {@link SimpleFireWallRule}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRules extends LinkedHashSet<SimpleFireWallRule> {

	private static final long serialVersionUID = -6844916283155922425L;

	public FireWallRules() {
		super();
	}

	public FireWallRules(FireWallRules rules) {
		super(rules);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("");
		for (SimpleFireWallRule rule : this) {
			str.append(SysTool.NEW_LINE + "firewall rule:" + rule);
		}
		return str.length() == 0 ? SysTool.NEW_LINE + "no firewall rules" : str
				.toString();
	}

	/**
	 * 
	 * @param target
	 * 
	 * @return a {@link FireWallRules}, which contains all
	 *         {@link SimpleFireWallRule} which are in the given target
	 *         {@link FireWallRules} and not in this object.
	 */
	public FireWallRules delta(FireWallRules target) {
		FireWallRules delta = new FireWallRules(target);
		delta.removeAll(this);
		return delta;
	}

}
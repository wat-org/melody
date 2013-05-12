package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.ex.Util;

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
		String res = "";
		for (SimpleFireWallRule rule : this) {
			res += Util.NEW_LINE + "firewall rule:" + rule;
		}
		return res.length() == 0 ? Util.NEW_LINE + "no firewall rules" : res;
	}

}
package com.wat.melody.cloud.firewall;

import com.wat.melody.common.firewall.FireWallRules;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesHelper {

	public static FireWallRules computeFireWallRulesToAdd(
			FireWallRules current, FireWallRules target) {
		FireWallRules networkToAdd = new FireWallRules(target);
		networkToAdd.removeAll(current);
		return networkToAdd;
	}

	public static FireWallRules computeFireWallRulesToRemove(
			FireWallRules current, FireWallRules target) {
		FireWallRules networkToRemove = new FireWallRules(current);
		networkToRemove.removeAll(target);
		return networkToRemove;
	}

}
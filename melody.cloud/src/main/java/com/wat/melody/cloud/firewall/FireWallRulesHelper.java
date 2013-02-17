package com.wat.melody.cloud.firewall;

import com.wat.melody.common.network.FwRulesDecomposed;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FireWallRulesHelper {

	public static FwRulesDecomposed computeFireWallRulesToAdd(
			FwRulesDecomposed current, FwRulesDecomposed target) {
		FwRulesDecomposed networkToAdd = new FwRulesDecomposed(target);
		networkToAdd.removeAll(current);
		return networkToAdd;
	}

	public static FwRulesDecomposed computeFireWallRulesToRemove(
			FwRulesDecomposed current, FwRulesDecomposed target) {
		FwRulesDecomposed networkToRemove = new FwRulesDecomposed(current);
		networkToRemove.removeAll(target);
		return networkToRemove;
	}

}

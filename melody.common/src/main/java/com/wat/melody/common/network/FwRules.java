package com.wat.melody.common.network;

import java.util.ArrayList;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRules extends ArrayList<FwRule> {

	private static final long serialVersionUID = 2186789098390749295L;

	public FwRules() {
		super();
	}

	/**
	 * <p>
	 * Convert each {@link FwRule} contained in this object into more fine grain
	 * {@link FwRuleDecomposed}.
	 * </p>
	 * 
	 * @return a Collection of {@link FwRuleDecomposed}.
	 */
	public FwRulesDecomposed decompose() {
		FwRulesDecomposed fws = new FwRulesDecomposed();
		for (FwRule fw : this) {
			fws.addAll(fw.decompose());
		}
		return fws;
	}

}

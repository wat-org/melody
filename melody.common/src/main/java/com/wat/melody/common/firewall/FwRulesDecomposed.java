package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRulesDecomposed extends LinkedHashSet<FwRuleDecomposed> {

	private static final long serialVersionUID = 2765454358787998895L;

	public FwRulesDecomposed() {
		super();
	}

	public FwRulesDecomposed(FwRulesDecomposed rl) {
		super(rl);
	}

}

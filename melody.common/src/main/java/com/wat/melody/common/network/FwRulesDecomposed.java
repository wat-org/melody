package com.wat.melody.common.network;

import java.util.ArrayList;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRulesDecomposed extends ArrayList<FwRuleDecomposed> {

	private static final long serialVersionUID = 2765454358787998895L;

	public FwRulesDecomposed() {
		super();
	}

	public FwRulesDecomposed(FwRulesDecomposed rl) {
		super(rl);
	}

	/**
	 * <p>
	 * Remove the {@link FwRuleDecomposed}'s doublon this object may contains.
	 * </p>
	 * 
	 * @return <code>this</code>.
	 */
	public FwRulesDecomposed simplify() {
		FwRuleDecomposed l_fw = null;
		FwRuleDecomposed r_fw = null;
		for (int l = 0; l < size(); l++) {
			l_fw = get(l);
			for (int r = size() - 1; r > l; r--) {
				r_fw = get(r);
				if (l_fw.equals(r_fw)) {
					remove(r);
				}
			}
		}
		return this;
	}

}

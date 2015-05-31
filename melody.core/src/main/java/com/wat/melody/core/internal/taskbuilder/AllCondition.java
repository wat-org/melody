package com.wat.melody.core.internal.taskbuilder;

import java.util.LinkedHashSet;

import org.w3c.dom.Element;

import com.wat.melody.api.ICondition;
import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AllCondition extends LinkedHashSet<ICondition> implements
		ICondition {

	private static final long serialVersionUID = -4798809865423798432L;

	/**
	 * @param elmt
	 * @param ps
	 * 
	 * @return <tt>true</tt> if all Conditions are true, or <tt>false</tt> if
	 *         any of the Condition is false.
	 */
	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		for (ICondition c : this) {
			if (!c.isEligible(elmt, ps)) {
				return false;
			}
		}
		return true;
	}

}
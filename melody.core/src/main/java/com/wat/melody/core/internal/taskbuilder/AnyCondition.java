package com.wat.melody.core.internal.taskbuilder;

import java.util.LinkedHashSet;

import org.w3c.dom.Element;

import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AnyCondition extends LinkedHashSet<ICondition> implements
		ICondition {

	private static final long serialVersionUID = -3798809865423798432L;

	/**
	 * @param elmt
	 * @param ps
	 * 
	 * @return <tt>false</tt> if all Conditions are false, or <tt>true</tt> if
	 *         any of the Condition is true.
	 */
	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		for (ICondition c : this) {
			if (c.isEligible(elmt, ps)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void markEligibleElements(Element elmt, PropertySet ps) {
		for (ICondition c : this) {
			c.markEligibleElements(elmt, ps);
		}
	}

}
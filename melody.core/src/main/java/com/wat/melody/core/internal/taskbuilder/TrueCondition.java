package com.wat.melody.core.internal.taskbuilder;

import org.w3c.dom.Element;

import com.wat.melody.api.ICondition;
import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TrueCondition implements ICondition {

	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		return true;
	}

}
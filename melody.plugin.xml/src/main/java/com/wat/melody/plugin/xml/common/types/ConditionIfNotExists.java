package com.wat.melody.plugin.xml.common.types;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConditionIfNotExists extends Condition {

	public boolean matches(Doc doc) {
		NodeList nl = null;
		try {
			nl = doc.evaluateAsNodeList(getCondition());
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("xpath expression have been already "
					+ "validated." + "Shouldn't happened."
					+ "A Bug must have been introduced.", Ex);
		}
		return nl == null || nl.getLength() == 0;
	}

}
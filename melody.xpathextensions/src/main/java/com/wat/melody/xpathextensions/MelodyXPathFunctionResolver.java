package com.wat.melody.xpathextensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

public class MelodyXPathFunctionResolver implements XPathFunctionResolver {

	static final QName f_getHeritedAttribute = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedAttribute.NAME);

	static final QName f_getHeritedContent = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedContent.NAME);

	/*
	 * TODO : find a way to add custom XPath Function via configuration file
	 */
	@Override
	public XPathFunction resolveFunction(QName qName, int arity) {
		if (qName.equals(f_getHeritedAttribute) && arity == 2) {
			return new GetHeritedAttribute();
		} else if (qName.equals(f_getHeritedContent) && arity == 2) {
			return new GetHeritedContent();
		}
		return null;
	}
}

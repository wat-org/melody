package com.wat.melody.xpathextensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

public class MelodyXPathFunctionResolver implements XPathFunctionResolver {

	static final QName f_getHeritedAttributeValue = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedAttribute.NAME);

	static final QName f_getHeritedContent = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedContent.NAME);

	static final QName f_getManagementNetworkInterface = new QName(
			CustomXPathFunctions.NAMESPACE_URI,
			GetManagementNetworkInterface.NAME);

	static final QName f_getManagementHost = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementHost.NAME);

	static final QName f_getManagementPort = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementPort.NAME);

	static final QName f_getManagementMethod = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementMethod.NAME);

	/*
	 * TODO : find a way to add custom XPath Function via configuration file
	 */
	@Override
	public XPathFunction resolveFunction(QName qName, int arity) {
		if (qName.equals(f_getHeritedAttributeValue) && arity == 2) {
			return new GetHeritedAttribute();
		} else if (qName.equals(f_getHeritedContent) && arity == 2) {
			return new GetHeritedContent();
		} else if (qName.equals(f_getManagementNetworkInterface) && arity == 1) {
			return new GetManagementNetworkInterface();
		} else if (qName.equals(f_getManagementHost) && arity == 1) {
			return new GetManagementHost();
		} else if (qName.equals(f_getManagementPort) && arity == 1) {
			return new GetManagementPort();
		} else if (qName.equals(f_getManagementMethod) && arity == 1) {
			return new GetManagementMethod();
		}
		return null;
	}
}

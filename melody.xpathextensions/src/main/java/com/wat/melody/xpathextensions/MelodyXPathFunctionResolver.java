package com.wat.melody.xpathextensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

public class MelodyXPathFunctionResolver implements XPathFunctionResolver {

	static final QName f_getHeritedAttributeValue = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedAttribute.NAME);

	static final QName f_getHeritedContent = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedContent.NAME);

	static final QName f_getNetworkManagementInterface = new QName(
			CustomXPathFunctions.NAMESPACE_URI,
			GetNetworkManagementInterface.NAME);

	static final QName f_getNetworkManagementHost = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetNetworkManagementHost.NAME);

	static final QName f_getNetworkManagementPort = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetNetworkManagementPort.NAME);

	static final QName f_getNetworkManagementMethod = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetNetworkManagementMethod.NAME);

	/*
	 * TODO : find a way to add custom XPath Function via configuration file
	 */
	@Override
	public XPathFunction resolveFunction(QName qName, int arity) {
		if (qName.equals(f_getHeritedAttributeValue) && arity == 2) {
			return new GetHeritedAttribute();
		} else if (qName.equals(f_getHeritedContent) && arity == 2) {
			return new GetHeritedContent();
		} else if (qName.equals(f_getNetworkManagementInterface) && arity == 1) {
			return new GetNetworkManagementInterface();
		} else if (qName.equals(f_getNetworkManagementHost) && arity == 1) {
			return new GetNetworkManagementHost();
		} else if (qName.equals(f_getNetworkManagementPort) && arity == 1) {
			return new GetNetworkManagementPort();
		} else if (qName.equals(f_getNetworkManagementMethod) && arity == 1) {
			return new GetNetworkManagementMethod();
		}
		return null;
	}
}

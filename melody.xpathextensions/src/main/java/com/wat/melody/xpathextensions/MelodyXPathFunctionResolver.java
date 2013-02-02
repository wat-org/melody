package com.wat.melody.xpathextensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MelodyXPathFunctionResolver implements XPathFunctionResolver {

	static final QName f_getHeritedAttributeValue = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedAttribute.NAME);

	static final QName f_getHeritedContent = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetHeritedContent.NAME);

	static final QName f_getNetworkManagementInterface = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementNetworkDevice.NAME);

	static final QName f_getManagementNetworkHost = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementNetworkHost.NAME);

	static final QName f_getManagementNetworkPort = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementNetworkPort.NAME);

	static final QName f_getManagementNetworkMethod = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetManagementNetworkMethod.NAME);

	static final QName f_getNetworkDevicByName = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetNetworkDeviceByName.NAME);

	static final QName f_getNetworkDevices = new QName(
			CustomXPathFunctions.NAMESPACE_URI, GetNetworkDevices.NAME);

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
			return new GetManagementNetworkDevice();
		} else if (qName.equals(f_getManagementNetworkHost) && arity == 1) {
			return new GetManagementNetworkHost();
		} else if (qName.equals(f_getManagementNetworkPort) && arity == 1) {
			return new GetManagementNetworkPort();
		} else if (qName.equals(f_getManagementNetworkMethod) && arity == 1) {
			return new GetManagementNetworkMethod();
		} else if (qName.equals(f_getNetworkDevicByName) && arity == 2) {
			return new GetNetworkDeviceByName();
		} else if (qName.equals(f_getNetworkDevices) && arity == 1) {
			return new GetNetworkDevices();
		}
		return null;
	}
}

package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;

import com.wat.melody.xpathextensions.common.ManagementInterfaceHelper;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public final class GetManagementNetworkInterface implements XPathFunction {

	public static final String NAME = "getManagementInterface";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node)) {
			throw new IllegalArgumentException(arg0.getClass()
					.getCanonicalName()
					+ ": Not accepted. "
					+ CustomXPathFunctions.NAMESPACE
					+ ":"
					+ NAME
					+ "() expects a Node " + "argument.");
		}
		try {
			return ManagementInterfaceHelper
					.getManagementNetworkInterface((Node) arg0);
		} catch (ResourcesDescriptorException Ex) {
			/*
			 * TODO : add the location of the Node in the error message
			 */
			throw new XPathFunctionException(Ex);
		}
	}

}

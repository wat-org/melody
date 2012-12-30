package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;

public class GetNetworkManagementHost implements XPathFunction {

	public static final String NAME = "getNetworkManagementHost";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node) && !(arg0 instanceof List)) {
			throw new IllegalArgumentException(arg0.getClass()
					.getCanonicalName()
					+ ": Not accepted. "
					+ CustomXPathFunctions.NAMESPACE
					+ ":"
					+ NAME
					+ "() expects a Node or a List<Node> argument.");
		}
		try {
			if (arg0 instanceof Node) {
				return NetworkManagementHelper
						.findNetworkManagementHost((Node) arg0);
			} else {
				// TODO : need some test
				return NetworkManagementHelper
						.findManagementNetworkHost((List<Node>) arg0);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}

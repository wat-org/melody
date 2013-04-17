package com.wat.melody.cloud.network.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.NetworkManagementHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public final class GetManagementNetworkDevice implements XPathFunction {

	public static final String NAME = "getManagementNetworkDevice";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node) && !(arg0 instanceof List)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME
					+ "() expects a Node or a List<Node> as first "
					+ "argument.");
		}
		try {
			if (arg0 instanceof Node) {
				return NetworkManagementHelper
						.findManagementNetworkDeviceNode((Node) arg0);
			} else {
				return NetworkManagementHelper
						.findManagementNetworkDeviceNode((List<Node>) arg0);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}

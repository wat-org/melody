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
public class GetManagementNetworkPort implements XPathFunction {

	public static final String NAME = "getManagementNetworkPort";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME
					+ "() expects a Node as first argument.");
		}
		try {
			return NetworkManagementHelper
					.findManagementNetworkPortNode((Node) arg0);
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}
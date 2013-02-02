package com.wat.melody.xpathextensions;

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
public class GetNetworkDevices implements XPathFunction {

	public static final String NAME = "getNetworkDevices";

	@SuppressWarnings({ "rawtypes" })
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		if (arg0 == null) {
			return null;
		}
		if (!(arg0 instanceof Node)) {
			throw new IllegalArgumentException(arg0.getClass()
					.getCanonicalName()
					+ ": Not accepted. "
					+ CustomXPathFunctions.NAMESPACE
					+ ":"
					+ NAME
					+ "() expects a Node as first argument.");
		}
		try {
			return NetworkManagementHelper.findNetworkDevices((Node) arg0);
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}

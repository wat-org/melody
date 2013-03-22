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
public class GetNetworkDeviceByName implements XPathFunction {

	public static final String NAME = "getNetworkDeviceByName";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		// accepts one or two args
		Object arg1 = null;
		if (list.size() > 1) {
			arg1 = list.get(1);
		}
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node) && !(arg0 instanceof List)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + CustomXPathFunctions.NAMESPACE + ":"
					+ NAME + "() expects a Node or a List<Node> as first "
					+ "argument.");
		}
		String arg1val = null;
		if (arg1 == null) {
			arg1val = null;
		} else {
			if (arg1 instanceof Node) {
				arg1val = ((Node) arg1).getNodeValue();
			} else if (arg1 instanceof String) {
				arg1val = (String) arg1;
			} else {
				throw new XPathFunctionException("null: Not accepted. "
						+ CustomXPathFunctions.NAMESPACE + ":" + NAME
						+ "() expects a String or a Node as second argument.");
			}
		}
		try {
			if (arg0 instanceof Node) {
				return NetworkManagementHelper.findNetworkDeviceNodeByName(
						(Node) arg0, arg1val);
			} else {
				return NetworkManagementHelper.findNetworkDeviceNodeByName(
						(List<Node>) arg0, arg1val);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}

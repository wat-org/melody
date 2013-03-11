package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpath.XPathHelper;

/**
 * <p>
 * XPath custom function, which return the value of the requested attribute,
 * from the given node or from the given node parents (via
 * {@link XPathHelper#HERIT_ATTR} XML attribute).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class GetHeritedAttribute implements XPathFunction {

	public static final String NAME = "getHeritedAttributeValue";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		Object arg1 = list.get(1);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (!(arg0 instanceof Node)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + CustomXPathFunctions.NAMESPACE + ":"
					+ NAME + "() expects a Node as first argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. "
					+ CustomXPathFunctions.NAMESPACE + ":" + NAME
					+ "() expects a non-null String as second argument.");
		}
		try {
			return XPathHelper.getHeritedAttributeValue((Node) arg0,
					(String) arg1);
		} catch (ResourcesDescriptorException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}

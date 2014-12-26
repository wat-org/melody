package com.wat.melody.common.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Attr;

/**
 * <p>
 * XPath custom function, which return the value of the requested attribute, or
 * return the given default value if the no attribute can be found.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class GetAttributeValue implements XPathFunction {

	public static final String NAME = "getAttributeValue";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		Object arg1 = list.get(1);
		if (arg0 != null && !(arg0 instanceof Attr) && !(arg0 instanceof List)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects a (null) "
					+ Attr.class.getCanonicalName() + " as first argument.");
		}
		if (arg0 instanceof List && ((List) arg0).size() != 0) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects a (null) "
					+ Attr.class.getCanonicalName() + " as first argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as second argument.");
		}

		if (arg0 instanceof List) {
			arg0 = null;
		} else if (arg0 instanceof Attr) {
			arg0 = ((Attr) arg0).getValue();
		}
		return (String) (arg0 != null ? arg0 : arg1);
	}

}
package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;

import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * <p>
 * XPath custom function, which return the value of the requested attribute,
 * from the given node or from the given node parents (via
 * {@link FilteredDoc#HERIT_ATTR} XML attribute).
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
		if (!(arg0 instanceof Element)) {
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME
					+ "() expects an Element Node as first argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as second argument.");
		}
		try {
			return XPathHelper.getHeritedAttributeValue((Element) arg0,
					(String) arg1);
		} catch (NodeRelatedException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}
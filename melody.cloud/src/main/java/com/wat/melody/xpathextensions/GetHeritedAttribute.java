package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;

import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		Object arg1 = list.get(1);
		if (arg0 == null || (arg0 instanceof List && ((List) arg0).size() == 0)) {
			return null;
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as second argument.");
		}
		try {
			if (XPathFunctionHelper.isElement(arg0)) {
				return XPathHelper.getHeritedAttributeValue((Element) arg0,
						(String) arg1, null);
			} else if (XPathFunctionHelper.isElementList(arg0)) {
				return XPathHelper.getHeritedAttributeValue(
						(List<Element>) arg0, (String) arg1, null);
			}
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects an "
					+ Element.class.getCanonicalName() + " or a "
					+ List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + "> as first "
					+ "argument.");
		} catch (NodeRelatedException | XPathExpressionException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}
package com.wat.melody.xpathextensions;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;

import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * <p>
 * XPath custom function, which evaluate the given XPath expression, relatively
 * to the given node and to the given node parents (via
 * {@link FilteredDoc#HERIT_ATTR} XML attribute).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class GetHeritedContent implements XPathFunction {

	public static final String NAME = "getHeritedContent";

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
				return FilteredDocHelper.getHeritedContent((Element) arg0,
						(String) arg1);
			} else if (XPathFunctionHelper.isElementList(arg0)) {
				return FilteredDocHelper.getHeritedContent(
						(List<Element>) arg0, (String) arg1);
			}
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects an "
					+ Element.class.getCanonicalName() + " or a "
					+ List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + "> as first "
					+ "argument.");
		} catch (XPathExpressionException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}
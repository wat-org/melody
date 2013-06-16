package com.wat.melody.xpathextensions;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.api.Melody;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

public class XPathHelper {

	/**
	 * <p>
	 * Look for given attribute in the given {@link Element} and its herited
	 * parents. Return the value of the first attribute found, where all XPath
	 * Expression have been expanded.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Element} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Element} and its herited parents.
	 * 
	 * @return the value of the first attribute found, where all XPath
	 *         Expression have been expanded.
	 * 
	 * @throws NodeRelatedException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression).
	 */
	public static String getHeritedAttributeValue(Element n, String sAttrName)
			throws NodeRelatedException {
		return getHeritedAttributeValue(n, sAttrName, true);
	}

	/**
	 * <p>
	 * Look for given attribute in the given {@link Element} and its herited
	 * parents. Return the value of the first attribute found. The returned
	 * value is expanded or not, regarding the value of the third boolean
	 * argument.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Element} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Element} and its herited parents.
	 * 
	 * @return the value of the first attribute found. The returned value is
	 *         expanded or not, regarding the value of the third boolean
	 *         argument.
	 * 
	 * @throws NodeRelatedException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression). This should never
	 *             happened if the third boolean argument is <tt>false</tt>.
	 */
	public static String getHeritedAttributeValue(Element n, String sAttrName,
			boolean expand) throws NodeRelatedException {
		Attr attr = FilteredDocHelper.getHeritedAttribute(n, sAttrName);
		if (attr == null) {
			return null;
		}
		String v = attr.getValue();
		if (!expand || v == null) {
			return v;
		}
		try {
			return Melody.getContext().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new NodeRelatedException(attr, Ex);
		}
	}

}
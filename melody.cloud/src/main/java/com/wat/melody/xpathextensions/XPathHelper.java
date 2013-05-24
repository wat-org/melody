package com.wat.melody.xpathextensions;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.api.Melody;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

public class XPathHelper {

	/**
	 * <p>
	 * Look for given attribute in the Node and its herited parents. Return the
	 * value of the first attribute found, where all XPath Expression have been
	 * expanded.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Node} and its herited parents.
	 * 
	 * @return the value of the first attribute found, where all XPath
	 *         Expression have been expanded.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression).
	 */
	public static String getHeritedAttributeValue(Element n, String sAttrName)
			throws ResourcesDescriptorException {
		return getHeritedAttributeValue(n, sAttrName, true);
	}

	/**
	 * <p>
	 * Look for given attribute in the Node and its herited parents. Return the
	 * value of the first attribute found. The returned value is expanded or
	 * not, regarding the value of the third boolean argument.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Node} and its herited parents.
	 * 
	 * @return the value of the first attribute found. The returned value is
	 *         expanded or not, regarding the value of the third boolean
	 *         argument.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression). This should not
	 *             happened if the third boolean argument is <tt>false</tt>.
	 */
	public static String getHeritedAttributeValue(Element n, String sAttrName,
			boolean expand) throws ResourcesDescriptorException {
		Node attr = FilteredDocHelper.getHeritedAttribute(n, sAttrName);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (!expand || v == null) {
			return v;
		}
		try {
			return Melody.getContext().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

}

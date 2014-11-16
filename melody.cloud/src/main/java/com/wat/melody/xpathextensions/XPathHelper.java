package com.wat.melody.xpathextensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.api.Melody;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

public class XPathHelper {

	public static List<String> getHeritedAttributeValue(List<Element> l,
			String expr, String defaultValue) throws NodeRelatedException,
			XPathExpressionException {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element n : l) {
			if (n == null) {
				continue;
			}
			String res = getHeritedAttributeValue(n, expr, defaultValue, true);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	/**
	 * @param n
	 *            is the {@link Element} to search in.
	 * @param expr
	 *            is a relative XPath Expression, which should query an XML
	 *            Attribute Node.
	 * @param defaultValue
	 *            is a default value, which will be returned if the given XPath
	 *            Expression doesn't match anything. Can be <tt>null</tt>.
	 * 
	 * @return The first {@link Attr} which match the given relative XPath
	 *         Expression, from the given {@link Element} and all herited
	 *         parents, or the given default value (which may be <tt>null</tt>),
	 *         if the given XPath Expression doesn't match anything. The
	 *         returned value is expanded.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given element is <tt>null</tt>, or if the given XPath
	 *             Expression is <tt>null</tt>, or if the given expression
	 *             doesn't match an XML Attribute Node.
	 * @throws XPathExpressionException
	 *             if the given XPath Expression is invalid.
	 * @throws NodeRelatedException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression).
	 */
	public static String getHeritedAttributeValue(Element n, String expr,
			String defaultValue) throws NodeRelatedException,
			XPathExpressionException {
		return getHeritedAttributeValue(n, expr, defaultValue, true);
	}

	/**
	 * @param n
	 *            is the {@link Element} to search in.
	 * @param expr
	 *            is a relative XPath Expression, which should query an XML
	 *            Attribute Node.
	 * @param defaultValue
	 *            is a default value, which will be returned if the given XPath
	 *            Expression doesn't match anything. Can be <tt>null</tt>.
	 * @param expand
	 *            is a boolean, which specifies if the first {@link Attr} which
	 *            match the given XPath Expression should be expanded or not.
	 * 
	 * @return The first {@link Attr} which match the given relative XPath
	 *         Expression, from the given {@link Element} and all herited
	 *         parents, or the given default value (which may be <tt>null</tt>),
	 *         if the given XPath Expression doesn't match anything. The
	 *         returned value is expanded or not, regarding the value of the
	 *         fourth boolean argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given element is <tt>null</tt>, or if the given XPath
	 *             Expression is <tt>null</tt>, or if the given expression
	 *             doesn't match an XML Attribute Node.
	 * @throws XPathExpressionException
	 *             if the given XPath Expression is invalid.
	 * @throws NodeRelatedException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression). This should never
	 *             happened if the fourth boolean argument is <tt>false</tt>.
	 */
	public static String getHeritedAttributeValue(Element n, String expr,
			String defaultValue, boolean expand) throws NodeRelatedException,
			XPathExpressionException {
		Attr attr = FilteredDocHelper
				.getHeritedAttribute(n, expr, defaultValue);
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
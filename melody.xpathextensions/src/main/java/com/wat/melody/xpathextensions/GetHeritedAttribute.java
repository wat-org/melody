package com.wat.melody.xpathextensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.common.Messages;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

/**
 * <p>
 * XPath custom function, which return the value of the requested attribute,
 * from the given node or from the given node parents (via
 * {@link CustomXPathFunctions#HERIT_ATTR} XML attribute).
 * </p>
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
			throw new IllegalArgumentException(arg0.getClass()
					.getCanonicalName()
					+ ": Not accepted. "
					+ CustomXPathFunctions.NAMESPACE
					+ ":"
					+ NAME
					+ "() expects a Node argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ CustomXPathFunctions.NAMESPACE + ":" + NAME
					+ "() expects a non-null String " + "argument.");
		}
		try {
			return getHeritedAttributeValue((Node) arg0, (String) arg1);
		} catch (ResourcesDescriptorException Ex) {
			/*
			 * TODO : add the location of the Node in the error message
			 */
			throw new XPathFunctionException(Ex);
		}
	}

	/*
	 * TODO : expand the resulting value, regarding the value of the third
	 * boolean arg
	 */
	public static String getHeritedAttributeValue(Node n, String sAttrName)
			throws ResourcesDescriptorException {
		Node attr = getHeritedAttribute(n, sAttrName);
		return attr == null ? null : attr.getNodeValue();
	}

	public static Node getHeritedAttribute(Node n, String sAttrName)
			throws ResourcesDescriptorException {
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		return getHeritedAttribute(n, sAttrName, circle);
	}

	private static Node getHeritedAttribute(Node n, String sAttrName,
			List<Node> circle) throws ResourcesDescriptorException {
		Node a = n.getAttributes().getNamedItem(sAttrName);
		if (a != null) {
			return a;
		}
		a = n.getAttributes().getNamedItem(CustomXPathFunctions.HERIT_ATTR);
		if (a == null) {
			return null;
		}
		String sXPathXpr = a.getNodeValue();
		if (sXPathXpr == null || sXPathXpr.length() == 0) {
			return null;
		}
		NodeList nl = null;
		try {
			nl = Doc.evaluateAsNodeList(sXPathXpr, n.getOwnerDocument()
					.getFirstChild());
		} catch (XPathExpressionException Ex) {
			throw new ResourcesDescriptorException(a, Messages.bind(
					Messages.RDEx_INVALID_HERIT_ATTR_XPATH, sXPathXpr), Ex);
		}
		if (nl.getLength() > 1) {
			throw new ResourcesDescriptorException(a, Messages.bind(
					Messages.RDEx_INVALID_HERIT_ATTR_MANYNODEMATCH, sXPathXpr));
		} else if (nl.getLength() == 0) {
			throw new ResourcesDescriptorException(a, Messages.bind(
					Messages.RDEx_INVALID_HERIT_ATTR_NONODEMATCH, sXPathXpr));
		}
		if (circle.contains(nl.item(0))) {
			throw new ResourcesDescriptorException(a, Messages.bind(
					Messages.RDEx_INVALID_HERIT_ATTR_CIRCULARREF, sXPathXpr));
		}
		circle.add(nl.item(0));
		return getHeritedAttribute(nl.item(0), sAttrName, circle);
	}

}

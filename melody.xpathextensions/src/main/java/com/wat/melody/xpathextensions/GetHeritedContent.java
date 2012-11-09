package com.wat.melody.xpathextensions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.common.Messages;

/**
 * <p>
 * XPath custom function, which evaluate the given XPath expression, relatively
 * to the given node and to the given node parents (via
 * {@link CustomXPathFunctions#HERIT_ATTR} XML attribute).
 * </p>
 */
public final class GetHeritedContent implements XPathFunction {

	public static final String NAME = "getHeritedContent";

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
					+ "() expects a NodeList " + "argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ CustomXPathFunctions.NAMESPACE + ":" + NAME
					+ "() expects a non-null String " + "argument.");
		}
		try {
			return getHeritedContent((Node) arg0, (String) arg1);
		} catch (ResourcesDescriptorException | XPathExpressionException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

	public static NodeList getHeritedContent(Node n, String sXPathExpr)
			throws XPathExpressionException, ResourcesDescriptorException {
		String refNodesXpr = Doc.getXPathPosition(n);
		String ref = null;
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		while (true) {
			Node a = n.getAttributes().getNamedItem(
					CustomXPathFunctions.HERIT_ATTR);
			if (a == null) {
				break;
			}
			ref = a.getNodeValue();
			if (ref == null || ref.length() == 0) {
				break;
			}
			refNodesXpr += " | " + ref;
			NodeList nl = null;
			try {
				nl = Doc.evaluateAsNodeList(ref, n.getOwnerDocument()
						.getFirstChild());
			} catch (XPathExpressionException Ex) {
				throw new ResourcesDescriptorException(a, Messages.bind(
						Messages.RDEx_INVALID_HERIT_ATTR_XPATH, ref), Ex);
			}
			if (nl.getLength() > 1) {
				throw new ResourcesDescriptorException(a, Messages.bind(
						Messages.RDEx_INVALID_HERIT_ATTR_MANYNODEMATCH, ref));
			} else if (nl.getLength() == 0) {
				throw new ResourcesDescriptorException(a, Messages.bind(
						Messages.RDEx_INVALID_HERIT_ATTR_NONODEMATCH, ref));
			}
			n = nl.item(0);
			if (circle.contains(n)) {
				throw new ResourcesDescriptorException(a, Messages.bind(
						Messages.RDEx_INVALID_HERIT_ATTR_CIRCULARREF, ref));
			}
			circle.add(n);
		}
		return Doc.evaluateAsNodeList("for $n in " + refNodesXpr + " "
				+ "return $n" + sXPathExpr, n.getOwnerDocument()
				.getFirstChild());
	}

}

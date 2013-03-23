package com.wat.melody.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.xpathextensions.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathHelper {

	/*
	 * TODO : put this in FilteredDoc ?
	 */

	/*
	 * TODO : since FilteredDoc perform the validation of the HERIT_ATTR, this
	 * code can be simplified
	 */
	public static NodeList getHeritedContent(Node n, String sXPathExpr)
			throws XPathExpressionException, ResourcesDescriptorException {
		String refNodesXpr = Doc.getXPathPosition(n);
		String ref = null;
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		while (true) {
			Node a = n.getAttributes().getNamedItem(FilteredDoc.HERIT_ATTR);
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

	public static String getHeritedAttributeValue(Node n, String sAttrName,
			boolean expand) throws ResourcesDescriptorException {
		Node attr = getHeritedAttribute(n, sAttrName);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (!expand || v == null) {
			return v;
		}
		try {
			return XPathExpander.expand(v,
					n.getOwnerDocument().getFirstChild(), null);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	public static String getHeritedAttributeValue(Node n, String sAttrName)
			throws ResourcesDescriptorException {
		return getHeritedAttributeValue(n, sAttrName, true);
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
		a = n.getAttributes().getNamedItem(FilteredDoc.HERIT_ATTR);
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

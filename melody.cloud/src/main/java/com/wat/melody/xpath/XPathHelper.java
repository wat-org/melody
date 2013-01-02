package com.wat.melody.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.utils.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathHelper {

	/**
	 * The 'herit' XML attribute of an XML Element in the Resources Descriptor
	 */
	public static final String HERIT_ATTR = "herit";

	public static NodeList getHeritedContent(Node n, String sXPathExpr)
			throws XPathExpressionException, ResourcesDescriptorException {
		String refNodesXpr = Doc.getXPathPosition(n);
		String ref = null;
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		while (true) {
			Node a = n.getAttributes().getNamedItem(HERIT_ATTR);
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
		a = n.getAttributes().getNamedItem(HERIT_ATTR);
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

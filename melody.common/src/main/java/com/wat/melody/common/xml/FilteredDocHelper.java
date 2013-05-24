package com.wat.melody.common.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.exception.FilteredDocException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDocHelper {

	public static NodeList getHeritedContent(Element n, String expr)
			throws XPathExpressionException {
		String refNodesXpr = Doc.getXPathPosition(n);
		Node ctx = n.getOwnerDocument().getFirstChild();
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		while (true) {
			try {
				n = resolvHeritAttr(n, circle);
			} catch (FilteredDocException Ex) {
				throw new RuntimeException("Unexecpted error while resolving "
						+ "herited attribute. "
						+ "Because all herited attributes have already been "
						+ "validated, such error cannot happened. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.", Ex);
			}
			if (n == null) {
				break;
			}
			refNodesXpr += " | " + Doc.getXPathPosition(n);
		}
		return XPathExpander.evaluateAsNodeList("for $n in " + refNodesXpr
				+ " " + "return $n" + expr, ctx);
	}

	public static Attr getHeritedAttribute(Element n, String sAttrName) {
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		return getHeritedAttribute(n, sAttrName, circle);
	}

	private static Attr getHeritedAttribute(Element n, String sAttrName,
			List<Element> circle) {
		if (n == null) {
			return null;
		}
		Attr a = n.getAttributeNode(sAttrName);
		if (a != null) {
			return a;
		}
		Element parent = null;
		try {
			parent = resolvHeritAttr(n, circle);
		} catch (FilteredDocException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited attribute. "
					+ "Because all herited attributes have already been "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		if (parent == null) {
			return null;
		}
		return getHeritedAttribute(parent, sAttrName, circle);
	}

	/**
	 * <p>
	 * Find the herited parent of the given {@link Node}.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to find its herited parent.
	 * @param circle
	 *            is the list of all already visited herited {@link Node}s It is
	 *            used to detect circular references. If <tt>null</tt>, circular
	 *            references will not be detected.
	 * 
	 * @throws FilteredDocException
	 *             if the content of the {@link FilteredDoc#HERIT_ATTR} XML
	 *             Attribute of the given {@link Node} is not a valid XPath
	 *             Expression.
	 * @throws FilteredDocException
	 *             if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Node} match no herited parent {@link Node}.
	 * @throws FilteredDocException
	 *             if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Node} match multiple herited parent {@link Node}s.
	 * @throws FilteredDocException
	 *             if, once resolved, the herited parent {@link Node} have
	 *             already been visited (e.g. a circular reference hae been
	 *             detected).
	 */
	protected static Element resolvHeritAttr(Element n, List<Element> circle)
			throws FilteredDocException {
		if (n == null) {
			return null;
		}
		Node herit = n.getAttributes().getNamedItem(FilteredDoc.HERIT_ATTR);
		if (herit == null) {
			return null;
		}
		String xpath = herit.getNodeValue();
		if (xpath == null || xpath.length() == 0) {
			return null;
		}
		NodeList nl = null;
		try {
			nl = XPathExpander.evaluateAsNodeList(xpath, n.getOwnerDocument()
					.getFirstChild());
		} catch (XPathExpressionException Ex) {
			throw new FilteredDocException(herit, Messages.bind(
					Messages.FilteredDocEx_INVALID_HERIT_ATTR_XPATH, xpath), Ex);
		}
		if (nl.getLength() > 1) {
			throw new FilteredDocException(herit, Messages.bind(
					Messages.FilteredDocEx_INVALID_HERIT_ATTR_MANYNODEMATCH,
					xpath));
		} else if (nl.getLength() == 0) {
			throw new FilteredDocException(herit, Messages.bind(
					Messages.FilteredDocEx_INVALID_HERIT_ATTR_NONODEMATCH,
					xpath));
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new FilteredDocException(
					herit,
					Messages.bind(
							Messages.FilteredDocEx_INVALID_HERIT_ATTR_NOTANELEMENTMATCH,
							xpath, Doc.parseNodeType(nl.item(0))));
		}
		Element parent = (Element) nl.item(0);

		if (circle != null) {
			if (circle.contains(parent)) {
				throw new FilteredDocException(herit, Messages.bind(
						Messages.FilteredDocEx_INVALID_HERIT_ATTR_CIRCULARREF,
						printCircularReferences(circle)));
			}
			circle.add(parent);
		}
		return parent;
	}

	private static String printCircularReferences(List<Element> circularRefStack) {
		StringBuilder str = new StringBuilder("");
		try {
			for (Element n : circularRefStack) {
				Element h = resolvHeritAttr(n, null);
				str.append(SysTool.NEW_LINE);
				str.append("  Node ");
				str.append("[" + Doc.getNodeLocation(n).toFullString() + "]");
				str.append(" herits of Node ");
				str.append("[" + Doc.getNodeLocation(h).toFullString() + "]");
			}
		} catch (FilteredDocException ignored) {
		}
		return str.toString();
	}

	/**
	 * <p>
	 * Import the given {@link Node} and all of its herited parents into the
	 * given {@link Document}.
	 * </p>
	 * 
	 * @param dest
	 *            is the {@link Document} to import {@link Node}s in.
	 * @param toClone
	 *            is the {@link Node} to import in the given {@link Document}.
	 *            All its herited parents will be imported too.
	 */
	protected static void importHeritedParentNode(Document dest, Element toClone) {
		if (toClone.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		Element parent = null;
		try {
			parent = resolvHeritAttr(toClone, null);
		} catch (FilteredDocException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited attribute. "
					+ "Because all herited attributes have already been "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		if (parent == null) {
			return;
		}
		FilteredDoc.importNodeIntoFilteredDocument(dest, parent, true);
	}

	/**
	 * <p>
	 * Validate content of {@link #HERIT_ATTR} XML attribute of the the given
	 * {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to validate.
	 * 
	 * @throws FilteredDocException
	 *             if the given {@link Document} contains {@link Node}s which
	 *             contains an invalid {@link #HERIT_ATTR} XML attribute
	 *             (circular ref, invalid xpath expr, no target, multiple
	 *             targets).
	 */
	protected static void validateParentHeritedNodes(Document doc)
			throws FilteredDocException {
		NodeList nl = findNodeWithHeritAttr(doc);
		if (nl.getLength() == 0) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			validateHeritAttr((Element) nl.item(i));
		}
	}

	/**
	 * <p>
	 * Find all {@link Node}s which contains an {@link #HERIT_ATTR} XML
	 * attribute in the given {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to search in.
	 * 
	 * @return a {@link NodeList}, where each item is a {@link Node} which
	 *         contains an {@link #HERIT_ATTR} XML attribute.
	 */
	public static NodeList findNodeWithHeritAttr(Document doc) {
		try {
			return XPathExpander.evaluateAsNodeList("//*[ exists(@"
					+ FilteredDoc.HERIT_ATTR + ") ]", doc);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Since the XPath expression to evaluate is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Validate content of {@link #HERIT_ATTR} XML attribute of the the given
	 * {@link Node} and of all its herted parents.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to validate.
	 * 
	 * @throws FilteredDocException
	 *             if the given {@link Node} or one of its herited parents
	 *             contains an invalid {@link #HERIT_ATTR} XML attribute
	 *             (circular ref, invalid xpath expr, no target, multiple
	 *             targets).
	 */
	private static void validateHeritAttr(Element n)
			throws FilteredDocException {
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		validateHeritAttr(n, circle);
	}

	private static void validateHeritAttr(Element n, List<Element> circle)
			throws FilteredDocException {
		Element parent = resolvHeritAttr(n, circle);
		if (parent == null) {
			return;
		}
		validateHeritAttr(parent, circle);
	}

}

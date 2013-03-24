package com.wat.melody.common.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.exception.FilteredDocException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDocHelper {

	public static NodeList getHeritedContent(Node n, String sXPathExpr)
			throws XPathExpressionException {
		String refNodesXpr = Doc.getXPathPosition(n);
		Node base = n.getOwnerDocument().getFirstChild();
		List<Node> circle = new ArrayList<Node>();
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
						+ "a bug have been introduced.");
			}
			if (n == null) {
				break;
			}
			refNodesXpr += " | " + Doc.getXPathPosition(n);
		}
		return Doc.evaluateAsNodeList("for $n in " + refNodesXpr + " "
				+ "return $n" + sXPathExpr, base);
	}

	public static Node getHeritedAttribute(Node n, String sAttrName) {
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		return getHeritedAttribute(n, sAttrName, circle);
	}

	private static Node getHeritedAttribute(Node n, String sAttrName,
			List<Node> circle) {
		Node a = n.getAttributes().getNamedItem(sAttrName);
		if (a != null) {
			return a;
		}
		Node parent = null;
		try {
			parent = resolvHeritAttr(n, circle);
		} catch (FilteredDocException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited attribute. "
					+ "Because all herited attributes have already been "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
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
	protected static Node resolvHeritAttr(Node n, List<Node> circle)
			throws FilteredDocException {
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
			nl = Doc.evaluateAsNodeList(xpath, n.getOwnerDocument()
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
		Node parent = nl.item(0);
		if (circle != null) {
			if (circle.contains(parent)) {
				throw new FilteredDocException(herit, Messages.bind(
						Messages.FilteredDocEx_INVALID_HERIT_ATTR_CIRCULARREF,
						xpath, Doc.getNodeLocation(nl.item(0)).toFullString()));
			}
			circle.add(parent);
		}
		return parent;
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
	protected static void importHeritedParentNode(Document dest, Node toClone) {
		if (toClone.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		Node parent = null;
		try {
			parent = FilteredDocHelper.resolvHeritAttr(toClone, null);
		} catch (FilteredDocException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited attribute. "
					+ "Because all herited attributes have already been "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
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
			validateHeritAttr(nl.item(i));
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
			return Doc.evaluateAsNodeList("//*[ exists(@"
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
	private static void validateHeritAttr(Node n) throws FilteredDocException {
		List<Node> circle = new ArrayList<Node>();
		circle.add(n);
		validateHeritAttr(n, circle);
	}

	private static void validateHeritAttr(Node n, List<Node> circle)
			throws FilteredDocException {
		Node parent = FilteredDocHelper.resolvHeritAttr(n, circle);
		if (parent == null) {
			return;
		}
		validateHeritAttr(parent, circle);
	}

}

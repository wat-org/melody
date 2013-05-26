package com.wat.melody.common.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.exception.NodeRelatedException;
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
		Element ctx = (Element) n.getOwnerDocument().getFirstChild();
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		while (true) {
			try {
				n = resolvHeritAttr(n, circle);
			} catch (NodeRelatedException Ex) {
				throw new RuntimeException("Unexecpted error while resolving "
						+ "herited parents of the Element ["
						+ Doc.getNodeLocation(n) + "]. "
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
		} catch (NodeRelatedException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited parents of the Element ["
					+ Doc.getNodeLocation(n) + "]. "
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
	 * Find the herited parent of the given {@link Element}.
	 * </p>
	 * 
	 * @param n
	 *            is an {@link Element}.
	 * @param circle
	 *            is the list of all already visited herited {@link Element}s.
	 *            It is used to detect circular references. If <tt>null</tt>,
	 *            circular references will not be detected.
	 * 
	 * @throws NodeRelatedException
	 *             if the content of the {@link FilteredDoc#HERIT_ATTR} XML
	 *             Attribute of the given {@link Element} is not a valid XPath
	 *             Expression.
	 * @throws NodeRelatedException
	 *             if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Element} match no herited parent {@link Element}.
	 * @throws NodeRelatedException
	 *             if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Element} match multiple herited parent {@link Element}
	 *             s.
	 * @throws NodeRelatedException
	 *             if, once resolved, the herited parent {@link Element} have
	 *             already been visited (e.g. a circular reference hae been
	 *             detected).
	 */
	protected static Element resolvHeritAttr(Element n, List<Element> circle)
			throws NodeRelatedException {
		if (n == null) {
			return null;
		}
		Attr herit = n.getAttributeNode(FilteredDoc.HERIT_ATTR);
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
			throw new NodeRelatedException(herit, Messages.bind(
					Messages.HeritAttrEx_INVALID_XPATH, xpath), Ex);
		}
		if (nl.getLength() > 1) {
			ConsolidatedException causes = new ConsolidatedException(
					Messages.bind(Messages.HeritAttrEx_MATCH_RESUME, xpath));
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new NodeRelatedException(node,
						Messages.HeritAttrEx_MATCH));
			}
			throw new NodeRelatedException(herit, causes);
		} else if (nl.getLength() == 0) {
			throw new NodeRelatedException(herit, Messages.bind(
					Messages.HeritAttrEx_NO_MATCH, xpath));
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(herit, Messages.bind(
					Messages.HeritAttrEx_DONT_MATCH_ELEMENT, xpath,
					Doc.parseNodeType(nl.item(0))));
		}
		Element parent = (Element) nl.item(0);

		if (circle != null) {
			if (circle.contains(parent)) {
				throw new NodeRelatedException(herit, Messages.bind(
						Messages.HeritAttrEx_CIRCULAR_REF,
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
				str.append("  Element ");
				str.append("[" + Doc.getNodeLocation(n).toFullString() + "]");
				str.append(" has parent Element ");
				str.append("[" + Doc.getNodeLocation(h).toFullString() + "]");
			}
		} catch (NodeRelatedException ignored) {
			/*
			 * circularRefStack contains already visited Element, so this error
			 * shouldn't raise
			 */
		}
		return str.toString();
	}

	/**
	 * <p>
	 * Import the given {@link Element} and all its herited parents
	 * {@link Element} into the given {@link Document}.
	 * </p>
	 * 
	 * @param dest
	 *            is a {@link Document}.
	 * @param toClone
	 *            is an {@link Element}. All its herited parents {@link Element}
	 *            will be imported in the given {@link Document}.
	 */
	protected static void importHeritedParentNode(Document dest, Element toClone) {
		Element parent = null;
		try {
			parent = resolvHeritAttr(toClone, null);
		} catch (NodeRelatedException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited parents of the Element ["
					+ Doc.getNodeLocation(toClone) + "]. "
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
	 * @throws NodeRelatedException
	 *             if the given {@link Document} contains {@link Element}s which
	 *             contains an invalid {@link #HERIT_ATTR} XML attribute
	 *             (circular ref, invalid xpath expr, no target, multiple
	 *             targets).
	 */
	protected static void validateParentHeritedNodes(Document doc)
			throws NodeRelatedException {
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
	 * Find all {@link Element}s which contains an {@link #HERIT_ATTR} XML
	 * attribute in the given {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to search in.
	 * 
	 * @return a {@link NodeList}, where each item is an {@link Element} which
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
	 * {@link Element} and of all its herted parents.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Element} to validate.
	 * 
	 * @throws NodeRelatedException
	 *             if the given {@link Element} or one of its herited parents
	 *             contains an invalid {@link #HERIT_ATTR} XML attribute
	 *             (circular ref, invalid xpath expr, no target, multiple
	 *             targets).
	 */
	private static void validateHeritAttr(Element n)
			throws NodeRelatedException {
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		validateHeritAttr(n, circle);
	}

	private static void validateHeritAttr(Element n, List<Element> circle)
			throws NodeRelatedException {
		Element parent = resolvHeritAttr(n, circle);
		if (parent == null) {
			return;
		}
		validateHeritAttr(parent, circle);
	}

}

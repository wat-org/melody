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
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FilteredDocHelper {

	/**
	 * @param n
	 *            is the context, used for XPath Expression evaluation.
	 * @param sAttrName
	 *            is a relative XPath Expression.
	 * 
	 * @return {@link Node}s which match the given relative XPath Expression,
	 *         from the given context and all herited parents.
	 */
	public static NodeList getHeritedContent(Element n, String expr)
			throws XPathExpressionException {
		String refNodesXpr = DocHelper.getXPathPosition(n);
		Element ctx = (Element) n.getOwnerDocument().getFirstChild();
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		while (true) {
			try {
				n = resolvHeritAttr(n, circle);
			} catch (NodeRelatedException Ex) {
				throw new RuntimeException("Unexecpted error while resolving "
						+ "herited parents of the Element ["
						+ DocHelper.getNodeLocation(n).toFullString() + "]. "
						+ "Because all herited attributes have already been "
						+ "validated, such error cannot happened. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.", Ex);
			}
			if (n == null) {
				break;
			}
			refNodesXpr += " | " + DocHelper.getXPathPosition(n);
		}
		return XPathExpander.evaluateAsNodeList("for $n in " + refNodesXpr
				+ " " + "return $n" + expr, ctx);
	}

	/**
	 * @param n
	 *            is an {@link Element}.
	 * @param sAttrName
	 *            is the attribute name to search.
	 * 
	 * @return the value of the given attribute, or <tt>null</tt> if the given
	 *         attribute was not found in given {@link Element} and all its
	 *         herited parents.
	 */
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
					+ DocHelper.getNodeLocation(n).toFullString() + "]. "
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
	 * @param n
	 *            is an {@link Element}. Can be <tt>null</tt>.
	 * @param circle
	 *            is the list of all already visited herited {@link Element}s.
	 *            It is used to detect circular references. If <tt>null</tt>,
	 *            circular references will not be detected.
	 * 
	 * @return the herited parent of the given {@link Element}., or
	 *         <tt>null</tt> if the given {@link Element} was <tt>null</tt>.
	 * 
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the content of the {@link FilteredDoc#HERIT_ATTR} XML
	 *             Attribute of the given {@link Element} is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Element} match no herited parent {@link Element} ;</li>
	 *             <li>if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Element} match multiple herited parent {@link Element}
	 *             s ;</li>
	 *             <li>if, once resolved, the content of the
	 *             {@link FilteredDoc#HERIT_ATTR} XML Attribute of the given
	 *             {@link Element} match a {@link Node} which is not an
	 *             {@link Element} ;</li>
	 *             <li>if, once resolved, the herited parent {@link Element}
	 *             have already been visited (e.g. a circular reference hae been
	 *             detected) ;</li>
	 *             </ul>
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
			throw new NodeRelatedException(herit, Msg.bind(
					Messages.HeritAttrEx_INVALID_XPATH, xpath), Ex);
		}
		if (nl.getLength() > 1) {
			ConsolidatedException causes = new ConsolidatedException(Msg.bind(
					Messages.HeritAttrEx_MATCH_RESUME, xpath));
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new NodeRelatedException(node,
						Messages.HeritAttrEx_MATCH));
			}
			throw new NodeRelatedException(herit, causes);
		} else if (nl.getLength() == 0) {
			throw new NodeRelatedException(herit, Msg.bind(
					Messages.HeritAttrEx_NO_MATCH, xpath));
		}
		if (nl.item(0).getNodeType() != Node.ELEMENT_NODE) {
			throw new NodeRelatedException(herit, Msg.bind(
					Messages.HeritAttrEx_DONT_MATCH_ELEMENT, xpath,
					DocHelper.parseNodeType(nl.item(0))));
		}
		Element parent = (Element) nl.item(0);

		if (circle != null) {
			if (circle.contains(parent)) {
				throw new NodeRelatedException(herit, Msg.bind(
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
				str.append("  Element [");
				str.append(DocHelper.getNodeLocation(n).toFullString());
				str.append("] has parent Element [");
				str.append(DocHelper.getNodeLocation(h).toFullString());
				str.append("]");
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
	 * Validate content of {@link #HERIT_ATTR} XML attribute of the the given
	 * {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             {@inheritDoc}
	 */
	protected static void validateParentHeritedNodes(Document doc)
			throws NodeRelatedException {
		NodeList nl = findNodeWithHeritAttr(doc);
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
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>.
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
	 *            is the {@link Element} to validate. Can be <tt>null</tt>.
	 * 
	 * @throws NodeRelatedException
	 *             {@inheritDoc}
	 */
	private static void validateHeritAttr(Element n)
			throws NodeRelatedException {
		List<Element> circle = new ArrayList<Element>();
		circle.add(n);
		validateHeritAttr(n, circle);
	}

	/**
	 * @param n
	 *            is the {@link Element} to validate. Can be <tt>null</tt>.
	 * @param circle
	 *            is the list of all already visited herited {@link Element}s.
	 *            It is used to detect circular references. If <tt>null</tt>,
	 *            circular references will not be detected.
	 * 
	 * @throws NodeRelatedException
	 *             {@inheritDoc}
	 */
	private static void validateHeritAttr(Element n, List<Element> circle)
			throws NodeRelatedException {
		Element parent = resolvHeritAttr(n, circle);
		if (parent == null) {
			return;
		}
		validateHeritAttr(parent, circle);
	}

	/**
	 * <p>
	 * Insert the given {@link Element}'s into the given destination
	 * {@link Document}.
	 * </p>
	 * 
	 * <ul>
	 * <li>Insert all the given {@link Element}'s parent {@link Element}s in the
	 * given destination {@link Document} ;</li>
	 * <li>Insert the given {@link Element}'s child if specified ;</li>
	 * <li>Insert all herited parent {@link Element} ;</li>
	 * </ul>
	 * 
	 * @param dest
	 *            is the destination {@link Document}.
	 * @param toImport
	 *            is the {@link Element} to import (it mustn't be owned by the
	 *            given {@link Document}).
	 * @param importChilds
	 * 
	 * @return the inserted {@link Element}.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given destination {@link Document} is
	 *             <tt>null</tt> ;</li>
	 *             <li>if the given {@link Element} to insert is <tt>null</tt> ;
	 *             </li>
	 *             </ul>
	 */
	protected static Element insertElement(Document dest, Element toImport,
			boolean importChilds) {
		if (toImport == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid " + Element.class.getCanonicalName() + ".");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid " + Document.class.getCanonicalName()
					+ ".");
		}

		Node imported = DUNIDDocHelper.getElement(dest,
				DUNIDDocHelper.getDUNID(toImport));
		if (imported != null) {
			/*
			 * Node can be already inserted, but without its child... so, we
			 * have to insert childs
			 */
			if (importChilds == true) {
				for (int i = 0; i < toImport.getChildNodes().getLength(); i++) {
					Node child = toImport.getChildNodes().item(i);
					if (child.getNodeType() != Node.ELEMENT_NODE)
						continue;
					insertElement(dest, (Element) child, true);
				}
			}
			return (Element) imported;
		}

		Node importedParent = dest;
		if (toImport.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
			importedParent = insertElement(dest,
					(Element) toImport.getParentNode(), false);
		}

		imported = importNodeSubTree(dest, toImport, importChilds);
		importedParent.appendChild(imported);
		return (Element) imported;
	}

	/**
	 * <p>
	 * Import the given {@link Node}'s into the given destination
	 * {@link Document}.
	 * </p>
	 * 
	 * <ul>
	 * <li>Insert all herited parent {@link Element}s in the given destination
	 * {@link Document} ;</li>
	 * <li>Import the given {@link Node}'s child if specified ;</li>
	 * </ul>
	 * 
	 * @param dest
	 *            is the destination {@link Document}.
	 * @param toImport
	 *            is the {@link Node} to import (it mustn't be owned by the
	 *            given destination {@link Document}).
	 * @param importChilds
	 * 
	 * @return the imported {@link Node}.
	 * 
	 * @throws NullPointerException
	 *             <ul>
	 *             <li>if the given destination {@link Document} is
	 *             <tt>null</tt> ;</li>
	 *             <li>if the given {@link Element} to import is <tt>null</tt> ;
	 *             </li>
	 *             </ul>
	 */
	private static Node importNodeSubTree(Document dest, Node toImport,
			boolean importChilds) {
		if (toImport.getNodeType() == Node.ELEMENT_NODE) {
			insertHeritedParents(dest, (Element) toImport);
		}

		Node imported = dest.importNode(toImport, false);
		if (importChilds) {
			for (int i = 0; i < toImport.getChildNodes().getLength(); i++) {
				Node child = toImport.getChildNodes().item(i);
				imported.appendChild(importNodeSubTree(dest, child, true));
			}
		}
		return imported;
	}

	/**
	 * <p>
	 * Insert the given {@link Element}'s herited parents {@link Element} into
	 * the given destination {@link Document}.
	 * </p>
	 * 
	 * @param dest
	 *            is the destination {@link Document}.
	 * @param toClone
	 *            is an {@link Element} (it mustn't be owned by the given
	 *            destination {@link Document}).
	 */
	private static void insertHeritedParents(Document dest, Element toClone) {
		Element parent = null;
		try {
			parent = resolvHeritAttr(toClone, null);
		} catch (NodeRelatedException Ex) {
			throw new RuntimeException("Unexecpted error while resolving "
					+ "herited parents of the Element ["
					+ DocHelper.getNodeLocation(toClone).toFullString() + "]. "
					+ "Because all herited attributes have already been "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		if (parent == null) {
			return;
		}
		insertElement(dest, parent, true);
	}

}
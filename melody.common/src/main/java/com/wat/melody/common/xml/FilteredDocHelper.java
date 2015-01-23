package com.wat.melody.common.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
	 *            is an {@link Element}. Can be <tt>null</tt>.
	 * @param circle
	 *            is the list of all already visited herited {@link Element}s.
	 *            It is used to detect circular references. If <tt>null</tt>,
	 *            circular references will not be detected.
	 * 
	 * @return the herited parent of the given {@link Element}, or <tt>null</tt>
	 *         if the given {@link Element} was <tt>null</tt>, or if the given
	 *         {@link Element} has no herited parent.
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
			ConsolidatedException causes = new ConsolidatedException();
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new NodeRelatedException(node,
						Messages.HeritAttrEx_MATCH));
			}
			throw new NodeRelatedException(herit, Msg.bind(
					Messages.HeritAttrEx_MATCH_RESUME, xpath), causes);
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
				str.append("  XML Element [");
				str.append(DocHelper.getNodeLocation(n).toFullString());
				str.append("] has parent XML Element [");
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
	 * Merge content of {@link #HERIT_ATTR} XML attribute of the the given
	 * {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to merge.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             {@inheritDoc}
	 */
	protected static void mergeHeritedContent(FilteredDoc fd)
			throws NodeRelatedException {
		Document doc = fd.getDocument();
		NodeList nl = findNodeWithHeritAttr(doc);
		for (int i = 0; i < nl.getLength(); i++) {
			Element n = (Element) nl.item(i);
			Element parent = resolvHeritAttr(n, null);
			if (parent == null) {
				/*
				 * shouldn't happened since nl contains only element with an
				 * herited attr
				 */
				continue;
			}
			// remove 'herit' attr in node
			n.removeAttribute(FilteredDoc.HERIT_ATTR);
			// clone the heritedparent (deep clone)
			Element hpc = (Element) doc.importNode(parent, true);
			// remove UUID in the heritedparentclone subtree
			NodeList uuidsToRemove = null;
			try {
				uuidsToRemove = XPathExpander.evaluateAsNodeList(
						"descendant-or-self::*/@" + DUNIDDoc.DUNID_ATTR, hpc);
			} catch (XPathExpressionException Ex) {
				throw new RuntimeException("Unexecpted error while evaluating "
						+ "an XPath Expression. "
						+ "Since the XPath expression to evaluate is "
						+ "hard coded, " + "such error cannot happened. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.", Ex);
			}
			for (Node uuidToRemove : new NodeCollection(uuidsToRemove)) {
				Attr attr = (Attr) uuidToRemove;
				attr.getOwnerElement().removeAttributeNode(attr);
			}
			// clone node, so that we keep user data
			Element nc = (Element) n.cloneNode(false);
			// remove all attrs in nodeclone
			while (nc.hasAttributes()) {
				nc.removeAttributeNode((Attr) nc.getAttributes().item(0));
			}
			// move heritedparentclone's child into nodeclone
			while (hpc.hasChildNodes()) {
				nc.appendChild(hpc.getFirstChild());
			}
			// copy heritedparentclone's attr into nodeclone
			for (int j = 0; j < hpc.getAttributes().getLength(); j++) {
				Attr attr = (Attr) hpc.getAttributes().item(j);
				nc.setAttribute(attr.getName(), attr.getValue());
			}
			// merge node into nodeclone
			mergeElement(n, nc, null);
			// replace node by nodeclone
			n.getParentNode().replaceChild(nc, n);
		}
		DUNIDDocHelper.addDUNID(doc.getFirstChild());
	}

	private static void mergeElement(Element source, Element dest,
			Element destParent) {
		if (dest == null) {
			destParent.appendChild(source);
			return;
		}

		// put source attrs into dest (source attr overrides dest attr)
		NamedNodeMap attrs = source.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			dest.setAttribute(attr.getName(), attr.getValue());
		}
		while (source.hasChildNodes()) {
			Node n = source.getFirstChild();
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				source.removeChild(n);
				continue;
			}
			Element sourcechild = (Element) n;
			Element destchild = null;
			if (sourcechild.hasAttribute("herit")) {
				dest.appendChild(sourcechild);
				continue;
			}
			String heritPolicy = sourcechild.getAttribute("herit-policy");
			sourcechild.removeAttribute("herit-policy");
			if (heritPolicy == null || heritPolicy.length() == 0
					|| heritPolicy.equals("merge")) {
				destchild = findMatchingChildElement(dest, sourcechild);
				Element scc = destchild;
				if (destchild != null) {
					// clone source child, so that we keep user data
					scc = (Element) sourcechild.cloneNode(false);
					// remove all sourcechildclone's attr
					while (scc.hasAttributes()) {
						scc.removeAttributeNode((Attr) scc.getAttributes()
								.item(0));
					}
					// clone destchild
					Element dcc = (Element) destchild.cloneNode(true);
					// move destchildclone's child into the sourcechildclone
					while (dcc.hasChildNodes()) {
						scc.appendChild(dcc.getFirstChild());
					}
					// copy destchildclone's attr into sourcechildclone
					for (int j = 0; j < dcc.getAttributes().getLength(); j++) {
						Attr attr = (Attr) dcc.getAttributes().item(j);
						scc.setAttribute(attr.getName(), attr.getValue());
					}
				}
				// merge sourcechild into sourcechildclone
				mergeElement(sourcechild, scc, dest);
				// replace destchild by sourcechildclone
				if (destchild != null) {
					dest.replaceChild(scc, destchild);
				}
				// remove sourcechild, so that next loop will act on next child
				if (sourcechild.getParentNode() == source) {
					source.removeChild(sourcechild);
				}
			} else if (heritPolicy.equals("append")) {
				dest.appendChild(sourcechild);
			} else if (heritPolicy.equals("insert")) {
				// TODO : currently equals to append.
				// should retrieve the insertion position
				Node refChild = null;
				dest.insertBefore(sourcechild, refChild);
			} else if (heritPolicy.equals("replace")) {
				destchild = findMatchingChildElement(dest, sourcechild);
				dest.replaceChild(sourcechild, destchild);
			}
		}
	}

	private static Element findMatchingChildElement(Element e, Element eToFind) {
		// TODO : replace by a more complex search, based on a xpath expr
		String toFind = eToFind.getNodeName();

		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (n.getNodeName().equals(toFind)) {
				return (Element) n;
			}
		}
		return null;
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

		return (Element) importNodeSubTree(dest, importedParent, toImport,
				importChilds);
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
	 * @param whereToImport
	 *            is the {@link Node} of the destination document which will
	 *            receive the imported {@link Node}.
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
	private static Node importNodeSubTree(Document dest, Node whereToImport,
			Node toImport, boolean importChilds) {
		if (toImport.getNodeType() == Node.ELEMENT_NODE) {
			insertHeritedParents(dest, (Element) toImport);
		}

		Node imported = dest.importNode(toImport, false);
		whereToImport.appendChild(imported);
		if (importChilds) {
			for (int i = 0; i < toImport.getChildNodes().getLength(); i++) {
				Node child = toImport.getChildNodes().item(i);
				importNodeSubTree(dest, imported, child, true);
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
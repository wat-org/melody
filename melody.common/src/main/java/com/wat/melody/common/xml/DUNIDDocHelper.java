package com.wat.melody.common.xml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.exception.IllegalDUNIDException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DUNIDDocHelper {

	/**
	 * @param d
	 *            is the {@link Document} to search in.
	 * 
	 * @return a {@link NodeList}, where each item is an {@link Element} which
	 *         contains a {@link #DUNID_ATTR} XML attribute.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link document} is <tt>null</tt>.
	 */
	public static NodeList findDUNIDs(Document d) {
		try {
			return XPathExpander.evaluateAsNodeList("//*[ exists(@"
					+ DUNIDDoc.DUNID_ATTR + ") ]", d);
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
	 * @param d
	 *            is the {@link Document} to search in, or <tt>null</tt>.
	 * @param dunid
	 *            is the {@link DUNID} to search, or <tt>null</tt>.
	 * 
	 * @return the {@link Element} whose {@link #DUNID_ATTR} XML attribute is
	 *         equal to the given input {@link DUNID}, or <tt>null</tt> if such
	 *         {@link Element} cannot be found, or if the given {@link Document}
	 *         is <tt>null</tt>, or if the given {@link DUNID} is <tt>null</tt>.
	 */
	public static Element getElement(Document d, DUNID dunid) {
		if (d == null || dunid == null) {
			return null;
		}
		try {
			return (Element) XPathExpander.evaluateAsNode("//*[@"
					+ DUNIDDoc.DUNID_ATTR + "='" + dunid.getValue() + "']", d);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * @param n
	 *            is an {@link Element}, or <tt>null</tt>.
	 * 
	 * @return the {@link DUNID} of the given {@link Element}, found in the
	 *         {@link #DUNID_ATTR} XML Attribute, or <tt>null</tt> if the given
	 *         {@link Element} is <tt>null</tt>.
	 * 
	 * @throws RuntimeException
	 *             if the given {@link Element} doens't have any
	 *             {@link #DUNID_ATTR} attribute, or if the value found in the
	 *             {@link #DUNID_ATTR} is not a valid {@link DUNID}.
	 */
	public static DUNID getDUNID(Element n) {
		if (n == null) {
			return null;
		}
		Attr a = n.getAttributeNode(DUNIDDoc.DUNID_ATTR);
		if (a == null) {
			throw new RuntimeException("The XML Element " + "["
					+ DocHelper.getNodeLocation(n).toFullString() + "] has no "
					+ "'" + DUNIDDoc.DUNID_ATTR + "' XML Attribute.");
		}
		String sDunid = a.getValue();
		try {
			return DUNID.parseString(sDunid);
		} catch (IllegalDUNIDException Ex) {
			throw new RuntimeException("The XML Element " + "["
					+ DocHelper.getNodeLocation(n).toFullString() + "] has a "
					+ "'" + DUNIDDoc.DUNID_ATTR + "' XML Attribute equals to '"
					+ sDunid + "', which is not valid.", Ex);
		}
	}

	// Add a DUNID Attribute to all child of the given Node
	protected static void addDUNID(Node n) {
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		Element e = (Element) n;
		NamedNodeMap oAttrList = e.getAttributes();

		// If the Node doesn't have a DUNID attribute
		if (oAttrList.getNamedItem(DUNIDDoc.DUNID_ATTR) == null) {
			// Search a Unique DUNID
			DUNID sDunid;
			Element oIsDunidAlreadyInserted;
			do {
				sDunid = new DUNID();
				oIsDunidAlreadyInserted = getElement(e.getOwnerDocument(),
						sDunid);
			} while (oIsDunidAlreadyInserted != null);
			// Add the DUNID attribute to the Node
			e.setAttribute(DUNIDDoc.DUNID_ATTR, sDunid.getValue());
		}
		// Repeat it for child Nodes
		for (int i = 0; i < n.getChildNodes().getLength(); i++) {
			addDUNID(e.getChildNodes().item(i));
		}
	}

}
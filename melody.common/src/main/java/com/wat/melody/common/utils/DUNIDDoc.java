package com.wat.melody.common.utils;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.utils.exception.DUNIDDocException;
import com.wat.melody.common.utils.exception.IllegalDUNIDException;
import com.wat.melody.common.utils.exception.IllegalDocException;
import com.wat.melody.common.utils.exception.NoSuchDUNIDException;

public class DUNIDDoc extends Doc {

	public static final String DUNID_ATTR = "__DUNID__";

	/**
	 * <p>
	 * Find all {@link Node}s which contains a {@link #DUNID_ATTR} XML attribute
	 * in the given {@link Document}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} to search in.
	 * 
	 * @return a {@link NodeList}, where each item is a {@link Node} which
	 *         contains a {@link #DUNID_ATTR} XML attribute.
	 */
	public static NodeList findDUNIDs(Document doc) {
		try {
			return evaluateAsNodeList("//*[ exists(@" + DUNID_ATTR + ") ]", doc);
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
	 * Find the {@link Node} whose {@link #DUNID_ATTR} XML attribute is equal to
	 * the given {@link DUNID}.
	 * </p>
	 * 
	 * @param base
	 *            is the {@link Document} to search in.
	 * @param dunid
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Node} whose {@link #DUNID_ATTR} XML attribute is equal
	 *         to the given input {@link DUNID}, or <code>null</code> if such
	 *         {@link Node} cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given given {@link Document} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 */
	public static Node getNode(Document base, DUNID dunid) {
		if (base == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		if (dunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DUNID.");
		}
		try {
			return evaluateAsNode(
					"//*[@" + DUNID_ATTR + "='" + dunid.getValue() + "']", base);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Get the {@link DUNID} of the given {@link Node}.
	 * </p>
	 * 
	 * @param n
	 *            is a {@link Node}.
	 * @return the {@link DUNID} of the given {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is <code>null</code>.
	 * @throws RuntimeException
	 *             if the given {@link Node} doens't have any
	 *             {@link #DUNID_ATTR} attribute, or if the value found in the
	 *             {@link #DUNID_ATTR} is not a valid {@link DUNID}.
	 */
	public synchronized static DUNID getDUNID(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		Attr a = (Attr) n.getAttributes().getNamedItem(DUNID_ATTR);
		if (a == null) {
			throw new RuntimeException("Unexpected error while retrieving the "
					+ "'" + DUNID_ATTR + "' XML Attribute of the element node "
					+ "which position is '" + getXPathPosition(n) + "'. "
					+ "Since a '" + DUNID_ATTR + "' XML Attribute have been "
					+ "previously added to all Node by the Melody's Engine, "
					+ "such error can't happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		try {
			return DUNID.parseString(a.getNodeValue());
		} catch (IllegalDUNIDException Ex) {
			throw new RuntimeException("Unexecpted error while creating a "
					+ "DUNID based a '" + DUNID_ATTR + "' XML Attribute's "
					+ "value. " + "Since this '" + DUNID_ATTR + "' XML "
					+ "Attribute's value have been automaticaly created by "
					+ "this object, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Get the attribute's value of the requested {@link Node}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} where the requested {@link Node} will
	 *            be searched.
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute.
	 * 
	 * @return a {@link String}, which contains the value of the requested
	 *         {@link Node}'s attribute, or <code>null</code> if this object
	 *         have not been loaded yet or if the requested {@link Node}'s
	 *         attribute doesn't exists.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any {@link node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public synchronized static String getAttributeValue(Document doc,
			DUNID ownerNodeDUNID, String sAttrName) throws NoSuchDUNIDException {
		if (sAttrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (doc != null) {
			Node n = getNode(doc, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Attr a = (Attr) n.getAttributes().getNamedItem(sAttrName);
			if (a != null) {
				return a.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Set the attribute's value of the requested {@link Node}. Create the
	 * attribute if it doesn't exist.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} where the requested {@link Node} will
	 *            be searched.
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute to set/create.
	 * @param sAttrValue
	 *            is the value to assign.
	 * 
	 * @return a {@link String}, which contains the previous value of the
	 *         requested {@link Node}'s attribute, or <code>null</code> if this
	 *         object have not been loaded yet or if the requested {@link Node}
	 *         's attribute didn't exists before the operation.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any {@link Node}.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public synchronized static String setAttributeValue(Document base,
			DUNID ownerNodeDUNID, String sAttrName, String sAttrValue)
			throws NoSuchDUNIDException {
		if (sAttrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (base != null) {
			Node n = getNode(base, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Attr a = (Attr) n.getAttributes().getNamedItem(sAttrName);
			if (a == null) {
				createAttribute(sAttrName, sAttrValue, n);
			} else {
				String previous = a.getNodeValue();
				a.setNodeValue(sAttrValue);
				return previous;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Remove the given attribute of the requested {@link Node}.
	 * </p>
	 * 
	 * @param doc
	 *            is the {@link Document} where the requested {@link Node} will
	 *            be searched.
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute to remove.
	 * 
	 * @return a {@link String}, which contains the previous value of the
	 *         {@link Node}'s attribute, or <code>null</code> if this object
	 *         have not been loaded yet or if the given attribute cannot be
	 *         found in the requested {@link Node}.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any node.
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public static synchronized String removeAttribute(Document doc,
			DUNID ownerNodeDUNID, String sAttrName) throws NoSuchDUNIDException {
		if (sAttrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (doc != null) {
			Node n = getNode(doc, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Node removed = null;
			try {
				removed = n.getAttributes().removeNamedItem(sAttrName);
			} catch (DOMException Ex) {
				// no need to raise anything
			}
			if (removed != null) {
				return removed.getNodeValue();
			}
		}
		return null;
	}

	// Add a DUNID Attribute to all child of the given Node
	private static void addDUNIDToNodeAndChildNodes(Node oNode, int index) {
		if (oNode.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		NamedNodeMap oAttrList = oNode.getAttributes();

		// If the Node doesn't have a DUNID attribute
		if (oAttrList.getNamedItem(DUNID_ATTR) == null) {
			// Search a Unique DUNID
			DUNID sDunid;
			Node oIsDunidAlreadyInserted;
			do {
				sDunid = new DUNID(index);
				oIsDunidAlreadyInserted = getNode(oNode.getOwnerDocument(),
						sDunid);
			} while (oIsDunidAlreadyInserted != null);
			// Add the DUNID to the Node
			createAttribute(DUNID_ATTR, sDunid.getValue(), oNode);
		}
		// Repeat it for child Nodes
		for (int i = 0; i < oNode.getChildNodes().getLength(); i++) {
			addDUNIDToNodeAndChildNodes(oNode.getChildNodes().item(i), index);
		}
	}

	private int miIndex;

	public DUNIDDoc() {
		this(0);
	}

	public DUNIDDoc(int index) {
		super();
		setIndex(index);
	}

	public int getIndex() {
		return miIndex;
	}

	private int setIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException(index + ": Not accpeted. "
					+ "Must be a positive integer or zero.");
		}
		int previous = getIndex();
		miIndex = index;
		return previous;
	}

	@Override
	protected synchronized void validateContent() throws IllegalDocException {
		super.validateContent();

		if (findDUNIDs(getDocument()).getLength() != 0) {
			throw new DUNIDDocException(Messages.bind(
					Messages.DUNIDDocEx_FOUND_DUNID_ATTR, getFileFullPath()));
		}

		addDUNIDToNodeAndChildNodes(getDocument().getFirstChild(), getIndex());
	}

	public synchronized void store() {
		store(getFileFullPath());
	}

	public synchronized void store(String sPath) {
		Document doc = (Document) getDocument().cloneNode(true);
		NodeList nl = findDUNIDs(doc);
		for (int i = 0; i < nl.getLength(); i++) {
			nl.item(i).getAttributes().removeNamedItem(DUNID_ATTR);
		}
		super.store(doc, sPath);
	}

	/**
	 * <p>
	 * Find the {@link Node} whose {@link #DUNID_ATTR} XML attribute is equal to
	 * the given {@link DUNID}.
	 * </p>
	 * 
	 * @param dunid
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Node} whose {@link #DUNID_ATTR} XML attribute is equal
	 *         to the given input {@link DUNID}, or <code>null</code> if such
	 *         {@link Node} cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if this object have not been loaded yet.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 */
	public synchronized Node getNode(DUNID dunid) {
		return getNode(getDocument(), dunid);
	}

	/**
	 * <p>
	 * Get the attribute's value of the requested {@link Node}.
	 * </p>
	 * 
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute.
	 * 
	 * @return a {@link String}, which contains the value of the requested
	 *         {@link Node}'s attribute, or <code>null</code> if this object
	 *         have not been loaded yet or if the requested {@link Node}'s
	 *         attribute doesn't exists.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any {@link node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public synchronized String getAttributeValue(DUNID ownerNodeDUNID,
			String sAttrName) throws NoSuchDUNIDException {
		return getAttributeValue(getDocument(), ownerNodeDUNID, sAttrName);
	}

	/**
	 * <p>
	 * Set the attribute's value of the requested {@link Node}. Create the
	 * attribute if it doesn't exist.
	 * </p>
	 * 
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute to set/create.
	 * @param sAttrValue
	 *            is the value to assign.
	 * 
	 * @return a {@link String}, which contains the previous value of the
	 *         requested {@link Node}'s attribute, or <code>null</code> if this
	 *         object have not been loaded yet or if the requested {@link Node}
	 *         's attribute didn't exists before the operation.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any {@link Node}.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public synchronized String setAttributeValue(DUNID ownerNodeDUNID,
			String sAttrName, String sAttrValue) throws NoSuchDUNIDException {
		return setAttributeValue(getDocument(), ownerNodeDUNID, sAttrName,
				sAttrValue);
	}

	/**
	 * <p>
	 * Remove the given attribute of the requested {@link Node}.
	 * </p>
	 * 
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the attribute to remove.
	 * 
	 * @return a {@link String}, which contains the previous value of the
	 *         {@link Node}'s attribute, or <code>null</code> if this object
	 *         have not been loaded yet or if the given attribute cannot be
	 *         found in the requested {@link Node}.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in the
	 *             {@link #DUNID_ATTR}'s attribute of any node.
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public synchronized String removeAttribute(DUNID ownerNodeDUNID,
			String sAttrName) throws NoSuchDUNIDException {
		return removeAttribute(getDocument(), ownerNodeDUNID, sAttrName);
	}

}

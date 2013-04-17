package com.wat.melody.common.xml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.exception.DUNIDDocException;
import com.wat.melody.common.xml.exception.IllegalDUNIDException;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DUNIDDoc extends Doc {

	public static final String DUNID_ATTR = "__DUNID__";

	/**
	 * <p>
	 * Find all {@link Node}s which contains a {@link #DUNID_ATTR} XML attribute
	 * in the given {@link Document}.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to search in.
	 * 
	 * @return a {@link NodeList}, where each item is a {@link Node} which
	 *         contains a {@link #DUNID_ATTR} XML attribute.
	 */
	public static NodeList findDUNIDs(Document d) {
		try {
			return XPathExpander.evaluateAsNodeList("//*[ exists(@"
					+ DUNID_ATTR + ") ]", d, null);
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
	 * @param d
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
	public static Node getNode(Document d, DUNID dunid) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		if (dunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DUNID.");
		}
		try {
			return XPathExpander.evaluateAsNode("//*[@" + DUNID_ATTR + "='"
					+ dunid.getValue() + "']", d, null);
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
	 * @return the {@link DUNID} of the given {@link Node}, found in the
	 *         {@link #DUNID_ATTR} XML Attribute.
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
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Must be valid Element Node.");
		}
		Node a = n.getAttributes().getNamedItem(DUNID_ATTR);
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
		String sDunid = a.getNodeValue();
		try {
			return DUNID.parseString(sDunid);
		} catch (IllegalDUNIDException Ex) {
			throw new RuntimeException("Unexecpted error while creating a "
					+ "DUNID based on the value '" + sDunid + "'. Since this "
					+ "value have been retrieved from the '" + DUNID_ATTR
					+ "' XML Attribute, which have been automaticaly "
					+ "created by this object, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Get the attribute's value of the requested {@link Node}.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} where the requested {@link Node} will
	 *            be searched.
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param attrName
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
	public synchronized static String getAttributeValue(Document d,
			DUNID ownerNodeDUNID, String attrName) throws NoSuchDUNIDException {
		if (attrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (d != null) {
			Node n = getNode(d, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Node a = n.getAttributes().getNamedItem(attrName);
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
	 * @param attrName
	 *            is the name of the attribute to set/create.
	 * @param attrValue
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
	public synchronized static String setAttributeValue(Document d,
			DUNID ownerNodeDUNID, String attrName, String attrValue)
			throws NoSuchDUNIDException {
		if (attrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (d != null) {
			Node n = getNode(d, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Node a = n.getAttributes().getNamedItem(attrName);
			if (a == null) {
				createAttribute(attrName, attrValue, n);
			} else {
				String previous = a.getNodeValue();
				a.setNodeValue(attrValue);
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
	 * @param d
	 *            is the {@link Document} where the requested {@link Node} will
	 *            be searched.
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param attrName
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
	public static synchronized String removeAttribute(Document d,
			DUNID ownerNodeDUNID, String attrName) throws NoSuchDUNIDException {
		if (attrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (d != null) {
			Node n = getNode(d, ownerNodeDUNID);
			if (n == null) {
				throw new NoSuchDUNIDException(Messages.bind(
						Messages.NoSuchDUNIDEx_UNFOUND,
						ownerNodeDUNID.getValue()));
			}
			Node removed = null;
			try {
				removed = n.getAttributes().removeNamedItem(attrName);
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
	private static void addDUNIDToNodeAndChildNodes(Node n, int index) {
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		NamedNodeMap oAttrList = n.getAttributes();

		// If the Node doesn't have a DUNID attribute
		if (oAttrList.getNamedItem(DUNID_ATTR) == null) {
			// Search a Unique DUNID
			DUNID sDunid;
			Node oIsDunidAlreadyInserted;
			do {
				sDunid = new DUNID(index);
				oIsDunidAlreadyInserted = getNode(n.getOwnerDocument(), sDunid);
			} while (oIsDunidAlreadyInserted != null);
			// Add the DUNID to the Node
			createAttribute(DUNID_ATTR, sDunid.getValue(), n);
		}
		// Repeat it for child Nodes
		for (int i = 0; i < n.getChildNodes().getLength(); i++) {
			addDUNIDToNodeAndChildNodes(n.getChildNodes().item(i), index);
		}
	}

	private int miIndex;
	private boolean mbHasChanged;

	public DUNIDDoc() {
		this(0);
	}

	public DUNIDDoc(int index) {
		super();
		setIndex(index);
		setHasChanged(false);
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

	private boolean hasChanged() {
		return mbHasChanged;
	}

	private boolean setHasChanged(boolean hasChanged) {
		boolean previous = hasChanged();
		mbHasChanged = hasChanged;
		return previous;
	}

	private void markHasChanged() {
		mbHasChanged = true;
	}

	/**
	 * <p>
	 * Raise an exception if one or more XML Element have a {@link #DUNID_ATTR}
	 * XML Attribute. {@link #DUNID_ATTR} XML Attribute is a reserved attribute,
	 * necessary for internal usage.
	 * </p>
	 * 
	 * @throws DUNIDDocException
	 *             if one or more XML Element have a {@link #DUNID_ATTR} XML
	 *             Attribute.
	 */
	@Override
	protected synchronized void validateContent() throws IllegalDocException {
		super.validateContent();

		NodeList nl = findDUNIDs(getDocument());
		if (nl.getLength() != 0) {
			throw new DUNIDDocException(nl.item(0), Messages.bind(
					Messages.DUNIDDocEx_FOUND_DUNID_ATTR, getFileFullPath()));
		}

		addDUNIDToNodeAndChildNodes(getDocument().getFirstChild(), getIndex());
	}

	/**
	 * <p>
	 * Store this object at the location that was used to load it.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will remove all added {link #DUNID_ATTR} XML Attributes ;</li>
	 * </ul>
	 */
	public synchronized void store() {
		store(getFileFullPath());
	}

	/**
	 * <p>
	 * Store this object at the given location.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will remove all added {link #DUNID_ATTR} XML Attributes ;</li>
	 * </ul>
	 */
	public synchronized void store(String sPath) {
		if (!hasChanged()) {
			return;
		}
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
	 * @param attrName
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
			String attrName) throws NoSuchDUNIDException {
		return getAttributeValue(getDocument(), ownerNodeDUNID, attrName);
	}

	/**
	 * <p>
	 * Set the attribute's value of the requested {@link Node}. Create the
	 * attribute if it doesn't exist.
	 * </p>
	 * 
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param attrName
	 *            is the name of the attribute to set/create.
	 * @param attrValue
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
			String attrName, String attrValue) throws NoSuchDUNIDException {
		String previous = setAttributeValue(getDocument(), ownerNodeDUNID,
				attrName, attrValue);
		if (previous == null || !attrValue.equals(previous)) {
			markHasChanged();
		}
		return previous;
	}

	/**
	 * <p>
	 * Remove the given attribute of the requested {@link Node}.
	 * </p>
	 * 
	 * @param ownerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param attrName
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
			String attrName) throws NoSuchDUNIDException {
		String previous = removeAttribute(getDocument(), ownerNodeDUNID,
				attrName);
		if (previous != null) {
			markHasChanged();
		}
		return previous;
	}

}

package com.wat.melody.common.xml;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.xml.exception.DUNIDDocException;
import com.wat.melody.common.xml.exception.IllegalDUNIDException;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DUNIDDoc extends Doc implements EventListener {

	private static Log log = LogFactory.getLog(DUNIDDoc.class);

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
					+ DUNID_ATTR + ") ]", d);
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
	 * Find the {@link Element} whose {@link #DUNID_ATTR} XML attribute is equal
	 * to the given {@link DUNID}.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to search in.
	 * @param dunid
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Element} whose {@link #DUNID_ATTR} XML attribute is
	 *         equal to the given input {@link DUNID}, or <tt>null</tt> if such
	 *         {@link Element} cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given given {@link Document} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <tt>null</tt>.
	 */
	public static Element getElement(Document d, DUNID dunid) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		if (dunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DUNID.");
		}
		try {
			return (Element) XPathExpander.evaluateAsNode("//*[@" + DUNID_ATTR
					+ "='" + dunid.getValue() + "']", d);
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
	 *             if the given {@link Node} is <tt>null</tt>.
	 * @throws RuntimeException
	 *             if the given {@link Node} doens't have any
	 *             {@link #DUNID_ATTR} attribute, or if the value found in the
	 *             {@link #DUNID_ATTR} is not a valid {@link DUNID}.
	 */
	public synchronized static DUNID getDUNID(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		Attr a = n.getAttributeNode(DUNID_ATTR);
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
		String sDunid = a.getValue();
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

	// Add a DUNID Attribute to all child of the given Node
	private static void addDUNIDToNodeAndChildNodes(Node n, int index) {
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		Element e = (Element) n;
		NamedNodeMap oAttrList = e.getAttributes();

		// If the Node doesn't have a DUNID attribute
		if (oAttrList.getNamedItem(DUNID_ATTR) == null) {
			// Search a Unique DUNID
			DUNID sDunid;
			Element oIsDunidAlreadyInserted;
			do {
				sDunid = new DUNID(index);
				oIsDunidAlreadyInserted = getElement(e.getOwnerDocument(),
						sDunid);
			} while (oIsDunidAlreadyInserted != null);
			// Add the DUNID attribute to the Node
			e.setAttribute(DUNID_ATTR, sDunid.getValue());
		}
		// Repeat it for child Nodes
		for (int i = 0; i < n.getChildNodes().getLength(); i++) {
			addDUNIDToNodeAndChildNodes(e.getChildNodes().item(i), index);
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

	@Override
	protected Document setDocument(Document d) {
		// Listen to all modifications performed on the underlying doc
		EventTarget target = (EventTarget) d;
		target.addEventListener("DOMAttrModified", this, true);
		target.addEventListener("DOMCharacterDataModified", this, true);
		target.addEventListener("DOMNodeRemoved", this, true);
		target.addEventListener("DOMNodeInserted", this, true);
		return super.setDocument(d);
		// TODO : should raise its own event on document modification
	}

	@Override
	public void handleEvent(Event evt) {
		if (!(evt instanceof MutationEvent)) {
			return;
		}
		try {
			MutationEvent e = (MutationEvent) evt;
			if (evt.getType().equals("DOMAttrModified")) {
				if (e.getNewValue() == null) {
					attributeRemoved(e);
				} else if (e.getPrevValue() == null) {
					attributeInserted(e);
				} else if (!e.getNewValue().equals(e.getPrevValue())) {
					attributeModified(e);
				}
			} else if (evt.getType().equals("DOMNodeInserted")
					&& evt.getTarget() instanceof Text) {
				nodeTextChanged(e);
			} else if (evt.getType().equals("DOMNodeInserted")) {
				nodeInstered(e);
			} else if (evt.getType().equals("DOMNodeRemoved")) {
				nodeRemoved(e);
			}
		} catch (Throwable Ex) {
			log.error(new MelodyException("Unexpected error while performing "
					+ "event propagation.", Ex).toString());
		}
	}

	protected void nodeInstered(MutationEvent evt) {
		markHasChanged();
	}

	protected void nodeRemoved(MutationEvent evt) {
		markHasChanged();
	}

	protected void nodeTextChanged(MutationEvent evt) {
		markHasChanged();
	}

	protected void attributeInserted(MutationEvent evt) {
		markHasChanged();
	}

	protected void attributeRemoved(MutationEvent evt) {
		markHasChanged();
	}

	protected void attributeModified(MutationEvent evt) {
		markHasChanged();
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
					Messages.DUNIDDocEx_FOUND_DUNID_ATTR, getSourceFile()));
		}

		addDUNIDToNodeAndChildNodes(getDocument().getFirstChild(), getIndex());
	}

	/**
	 * <p>
	 * Store this object into the given file.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will remove all added {link #DUNID_ATTR} XML Attributes ;</li>
	 * </ul>
	 * 
	 * @param path
	 *            is a file path, which specifies where the given
	 *            {@link Document} will be stored.
	 * 
	 * @throws IllegalFileException
	 *             if the given path points to a directory.
	 * @throws IllegalFileException
	 *             if the given path points to a non readable file.
	 * @throws IllegalFileException
	 *             if the given path points to a non writable file.
	 * @throws IllegalDirectoryException
	 *             if the given file's parent directory is not a readable
	 *             directory.
	 * @throws IllegalDirectoryException
	 *             if the given file's parent directory is not a writable
	 *             directory.
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 */
	@Override
	public synchronized void store(String sPath) throws IllegalFileException,
			IllegalDirectoryException {
		if (!hasChanged()) {
			return;
		}
		Document doc = (Document) getDocument().cloneNode(true);
		NodeList nl = findDUNIDs(doc);
		for (int i = 0; i < nl.getLength(); i++) {
			((Element) nl.item(i)).removeAttribute(DUNID_ATTR);
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
	 *         to the given input {@link DUNID}, or <tt>null</tt> if such
	 *         {@link Node} cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if this object have not been loaded yet.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <tt>null</tt>.
	 */
	public synchronized Element getElement(DUNID dunid) {
		return getElement(getDocument(), dunid);
	}

}
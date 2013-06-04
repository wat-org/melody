package com.wat.melody.common.xml;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * <p>
 * A {@link DUNIDDoc} is a {@link Document}, where an ID is added to each
 * {@link Element}. It exposes method to query {@link Element} by this ID.
 * </p>
 * 
 * <p>
 * <b>DUNID</b> stands for <b>D</b>ocument <b>UN</b>ique <b>ID</b>entifier.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class DUNIDDoc extends Doc implements EventListener {

	private static Log log = LogFactory.getLog(DUNIDDoc.class);

	public static final String DUNID_ATTR = "__DUNID__";

	private boolean _hasChanged;
	private Set<DocListener> _listeners;

	public DUNIDDoc() {
		super();
		setHasChanged(false);
		setListeners(new LinkedHashSet<DocListener>());
	}

	@Override
	public String toString() {
		return "{ " + "file:" + getSourceFile() + ", modified:" + hasChanged()
				+ " }";
	}

	private boolean hasChanged() {
		return _hasChanged;
	}

	private boolean setHasChanged(boolean hasChanged) {
		boolean previous = hasChanged();
		_hasChanged = hasChanged;
		return previous;
	}

	private void markHasChanged() {
		_hasChanged = true;
	}

	public Set<DocListener> getListeners() {
		return _listeners;
	}

	public Set<DocListener> setListeners(Set<DocListener> l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ DocListener.class.getCanonicalName() + ">.");
		}
		Set<DocListener> previous = _listeners;
		_listeners = l;
		return previous;
	}

	@Override
	protected Document setDocument(Document d) {
		super.setDocument(d);
		// Listen to all modifications performed on the underlying doc
		startListening();
		return getDocument();
	}

	public boolean addListener(DocListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + DocListener.class.getCanonicalName()
					+ ".");
		}
		return _listeners.add(l);
	}

	public boolean removeListener(DocListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + DocListener.class.getCanonicalName()
					+ ".");
		}
		return _listeners.remove(l);
	}

	private void fireNodeInsertedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.nodeInstered(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireNodeRemovedEvent(MutationEvent evt) throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.nodeRemoved(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireNodeTextChangedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.nodeTextChanged(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireAttributeInsertedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.attributeInserted(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireAttributeRemovedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.attributeRemoved(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireAttributeModifiedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.attributeModified(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	protected void startListening() {
		EventTarget target = (EventTarget) getDocument();
		target.addEventListener("DOMAttrModified", this, true);
		target.addEventListener("DOMCharacterDataModified", this, true);
		target.addEventListener("DOMNodeRemoved", this, true);
		target.addEventListener("DOMNodeInserted", this, true);
	}

	protected void stopListening() {
		EventTarget target = (EventTarget) getDocument();
		target.removeEventListener("DOMAttrModified", this, true);
		target.removeEventListener("DOMCharacterDataModified", this, true);
		target.removeEventListener("DOMNodeRemoved", this, true);
		target.removeEventListener("DOMNodeInserted", this, true);
	}

	@Override
	public void handleEvent(Event evt) {
		if (!(evt instanceof MutationEvent)) {
			return;
		}
		MutationEvent e = (MutationEvent) evt;
		try {
			if (evt.getType().equals("DOMAttrModified")) {
				if (e.getNewValue() == null) {
					attributeRemoved(e);
					fireAttributeRemovedEvent(e);
				} else if (e.getPrevValue() == null) {
					attributeInserted(e);
					fireAttributeInsertedEvent(e);
				} else if (!e.getNewValue().equals(e.getPrevValue())) {
					attributeModified(e);
					fireAttributeModifiedEvent(e);
				}
			} else if (evt.getType().equals("DOMNodeInserted")
					&& evt.getTarget() instanceof Text) {
				nodeTextChanged(e);
				fireNodeTextChangedEvent(e);
			} else if (evt.getType().equals("DOMNodeInserted")) {
				nodeInstered(e);
				fireNodeInsertedEvent(e);
			} else if (evt.getType().equals("DOMNodeRemoved")) {
				nodeRemoved(e);
				fireNodeRemovedEvent(e);
			}
		} catch (MelodyException Ex) {
			log.fatal(new MelodyException(Messages.bind(
					Messages.DUNIDDocEx_FORBIDDEN_OP, DocHelper
							.getNodeLocation((Node) e.getTarget())
							.toFullString()), Ex).toString());
		} catch (Throwable Ex) {
			log.fatal(new MelodyException(Messages.bind(
					Messages.DUNIDDocEx_UNEXPECTED_ERR, DocHelper
							.getNodeLocation((Node) e.getTarget())
							.toFullString()), Ex).toString());
		}
	}

	protected String getSmartMsg() {
		return "file:" + getSourceFile();
	}

	protected void nodeInstered(MutationEvent evt) throws MelodyException {
		Element t = (Element) evt.getTarget();
		// ensure the node (and its child) have a dunid attribute
		stopListening();
		DUNIDDocHelper.addDUNID(t);
		startListening();

		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_NODE_INSERTED,
				getSmartMsg(), ((Element) evt.getTarget()).getNodeName()));
	}

	protected void nodeRemoved(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_NODE_REMOVED,
				getSmartMsg(), ((Element) evt.getTarget()).getNodeName()));
	}

	protected void nodeTextChanged(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_NODE_TEXT_CHANGED,
				getSmartMsg(), ((Text) evt.getTarget()).getParentNode()
						.getNodeName(), ((Text) evt.getTarget())
						.getTextContent()));
	}

	protected void attributeInserted(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(),
					Messages.bind(Messages.DUNIDDocEx_DUNID_ADD, DUNID_ATTR,
							evt.getNewValue()));
		}
		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_ATTRIBUTE_INSERTED,
				getSmartMsg(), ((Element) evt.getTarget()).getNodeName(),
				evt.getNewValue()));
	}

	protected void attributeRemoved(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(),
					Messages.bind(Messages.DUNIDDocEx_DUNID_DEL, DUNID_ATTR));
		}
		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_ATTRIBUTE_REMOVED,
				getSmartMsg(), ((Element) evt.getTarget()).getNodeName(),
				evt.getPrevValue()));
	}

	protected void attributeModified(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(),
					Messages.bind(Messages.DUNIDDocEx_DUNID_MOD, DUNID_ATTR,
							evt.getNewValue()));
		}
		markHasChanged();
		log.debug(Messages.bind(Messages.DUNIDDocMsg_ATTRIBUTE_MODIFIED,
				getSmartMsg(), ((Element) evt.getTarget()).getNodeName(),
				evt.getPrevValue(), evt.getNewValue()));
	}

	/**
	 * @throws IllegalDocException
	 *             if one or more {@link Element}s have a {@link #DUNID_ATTR}
	 *             XML Attribute. {@link #DUNID_ATTR} XML Attribute is a
	 *             reserved attribute, necessary for internal usage.
	 */
	@Override
	protected synchronized void validateContent() throws IllegalDocException {
		super.validateContent();

		NodeList nl = DUNIDDocHelper.findDUNIDs(getDocument());
		if (nl.getLength() != 0) {
			ConsolidatedException causes = new ConsolidatedException(
					Messages.bind(Messages.DUNIDDocEx_FOUND_DUNID_RESUME,
							DUNID_ATTR));
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new NodeRelatedException(node, Messages.bind(
						Messages.DUNIDDocEx_FOUND_DUNID, DUNID_ATTR)));
			}
			throw new IllegalDocException(causes);
		}
		// When adding DUNID attributes, we don't want to listen/raise events
		stopListening();
		DUNIDDocHelper.addDUNID(getDocument().getFirstChild());
		startListening();
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
	 *            is a file path, which specifies where this object will be
	 *            stored.
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
		NodeList nl = DUNIDDocHelper.findDUNIDs(doc);
		for (int i = 0; i < nl.getLength(); i++) {
			((Element) nl.item(i)).removeAttribute(DUNID_ATTR);
		}
		DocHelper.store(doc, sPath);
	}

	/**
	 * @param dunid
	 *            is the {@link DUNID} to search, or <tt>null</tt>.
	 * 
	 * @return the {@link Element} whose {@link #DUNID_ATTR} XML attribute is
	 *         equal to the given input {@link DUNID}, or <tt>null</tt> if such
	 *         {@link Element} cannot be found or if the given {@link DUNID} is
	 *         <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if this object have not been loaded yet.
	 */
	public synchronized Element getElement(DUNID dunid) {
		return DUNIDDocHelper.getElement(getDocument(), dunid);
	}

}
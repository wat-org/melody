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
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * <p>
 * A {@link DUNIDDoc} is a {@link Document}, where a {@link DUNID} is added to
 * each {@link Element} as a reserved attribute. This class exposes methods to
 * query {@link Element}s via this {@link DUNID}.
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

	public synchronized String fulldump() {
		StringBuilder str = new StringBuilder();
		str.append("[file:");
		str.append(getSourceFile());
		str.append(']');
		str.append("\n| ");
		// even on Windows OS, new line contained in this string is '\n' ...
		str.append(dump().replaceAll("\\n", "\n| "));
		return str.toString();
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
		// Release the listener
		stopListening();
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

	private void fireElementInsertedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.elementInstered(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireElementRemovedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.elementRemoved(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireTextLeafInsertedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.textLeafInserted(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireTextLeafRemovedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.textLeafRemoved(evt);
			} catch (MelodyException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw cex;
		}
	}

	private void fireTextLeafModifiedEvent(MutationEvent evt)
			throws MelodyException {
		ConsolidatedException cex = new ConsolidatedException();
		for (DocListener l : getListeners()) {
			try {
				l.textLeafModified(evt);
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
		if (getDocument() == null) {
			return;
		}
		EventTarget target = (EventTarget) getDocument();
		target.addEventListener("DOMAttrModified", this, true);
		target.addEventListener("DOMCharacterDataModified", this, true);
		target.addEventListener("DOMNodeRemoved", this, true);
		target.addEventListener("DOMNodeInserted", this, true);
	}

	protected void stopListening() {
		if (getDocument() == null) {
			return;
		}
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
			} else if (evt.getTarget() instanceof Text) {
				if (((Text) evt.getTarget()).getParentNode().getChildNodes()
						.getLength() != 1) {
					if (evt.getType().equals("DOMCharacterDataModified")
							&& !e.getNewValue().equals(e.getPrevValue())) {
						textModified(e);
					} else if (evt.getType().equals("DOMNodeInserted")) {
						textInserted(e);
					} else if (evt.getType().equals("DOMNodeRemoved")) {
						textRemoved(e);
					}
				} else if (evt.getType().equals("DOMCharacterDataModified")
						&& !e.getNewValue().equals(e.getPrevValue())) {
					textLeafModified(e);
					fireTextLeafModifiedEvent(e);
				} else if (evt.getType().equals("DOMNodeInserted")) {
					textLeafInserted(e);
					fireTextLeafInsertedEvent(e);
				} else if (evt.getType().equals("DOMNodeRemoved")) {
					textLeafRemoved(e);
					fireTextLeafRemovedEvent(e);
				}
			} else if (evt.getType().equals("DOMNodeInserted")) {
				elementInstered(e);
				fireElementInsertedEvent(e);
			} else if (evt.getType().equals("DOMNodeRemoved")) {
				elementRemoved(e);
				fireElementRemovedEvent(e);
			}
		} catch (MelodyException Ex) {
			log.fatal(new NodeRelatedException((Node) e.getTarget(),
					Messages.DUNIDDocEx_FORBIDDEN_OP, Ex).toString());
		} catch (Throwable Ex) {
			log.fatal(new NodeRelatedException((Node) e.getTarget(),
					Messages.DUNIDDocEx_UNEXPECTED_ERR, Ex).toString());
		}
	}

	protected String getSmartMsg() {
		return "file:" + getSourceFile();
	}

	protected void textInserted(MutationEvent evt) throws MelodyException {
		Text t = (Text) evt.getTarget();
		// discard modification, if the target text node is not a leaf
		throw new NodeRelatedException(t, Msg.bind(
				Messages.DUNIDDocEx_TEXT_INSERTED, t.getTextContent()));
	}

	protected void textRemoved(MutationEvent evt) throws MelodyException {
		Text t = (Text) evt.getTarget();
		// discard modification, if the target text node is not a leaf
		throw new NodeRelatedException(t, Msg.bind(
				Messages.DUNIDDocEx_TEXT_REMOVED, t.getTextContent()));
	}

	protected void textModified(MutationEvent evt) throws MelodyException {
		// discard modification, if the target text node is not a leaf
		throw new NodeRelatedException((Node) evt.getTarget(), Msg.bind(
				Messages.DUNIDDocEx_TEXT_MODIFIED, evt.getPrevValue(),
				evt.getNewValue()));
	}

	protected void elementInstered(MutationEvent evt) throws MelodyException {
		Element t = (Element) evt.getTarget();
		// ensure the node (and its child) have a dunid attribute
		stopListening();
		DUNIDDocHelper.addDUNID(t);
		startListening();

		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_ELEMENT_INSERTED,
				getSmartMsg(), t.getNodeName()));
	}

	protected void elementRemoved(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_ELEMENT_REMOVED, getSmartMsg(),
				((Element) evt.getTarget()).getNodeName()));
	}

	protected void textLeafInserted(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_LEAF_TEXT_INSERTED,
				getSmartMsg(), ((Text) evt.getTarget()).getParentNode()
						.getNodeName(), ((Text) evt.getTarget())
						.getTextContent()));
	}

	protected void textLeafRemoved(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_LEAF_TEXT_REMOVED,
				getSmartMsg(), ((Text) evt.getTarget()).getParentNode()
						.getNodeName(), ((Text) evt.getTarget())
						.getTextContent()));
	}

	protected void textLeafModified(MutationEvent evt) throws MelodyException {
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_LEAF_TEXT_MODIFIED,
				getSmartMsg(), ((Text) evt.getTarget()).getParentNode()
						.getNodeName(), evt.getPrevValue(), evt.getNewValue()));
	}

	protected void attributeInserted(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(), Msg.bind(
					Messages.DUNIDDocEx_DUNID_ADD, DUNID_ATTR,
					evt.getNewValue()));
		}
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_ATTRIBUTE_INSERTED,
				getSmartMsg(), evt.getAttrName(), evt.getNewValue()));
	}

	protected void attributeRemoved(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(), Msg.bind(
					Messages.DUNIDDocEx_DUNID_DEL, DUNID_ATTR,
					evt.getPrevValue()));
		}
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_ATTRIBUTE_REMOVED,
				getSmartMsg(), evt.getAttrName(), evt.getPrevValue()));
	}

	protected void attributeModified(MutationEvent evt) throws MelodyException {
		if (evt.getAttrName().equals(DUNID_ATTR)) {
			throw new NodeRelatedException((Node) evt.getTarget(), Msg.bind(
					Messages.DUNIDDocEx_DUNID_MOD, DUNID_ATTR,
					evt.getPrevValue(), evt.getNewValue()));
		}
		markHasChanged();
		log.debug(Msg.bind(Messages.DUNIDDocMsg_ATTRIBUTE_MODIFIED,
				getSmartMsg(), evt.getAttrName(), evt.getPrevValue(),
				evt.getNewValue()));
	}

	/**
	 * @throws IllegalDocException
	 *             if one or more {@link Element}s have a {@link #DUNID_ATTR}
	 *             XML Attribute. {@link #DUNID_ATTR} XML Attribute is a
	 *             reserved attribute, necessary for internal usage.
	 */
	@Override
	protected void validateContent() throws IllegalDocException {
		super.validateContent();

		NodeList nl = DUNIDDocHelper.findDUNIDs(getDocument());
		if (nl.getLength() != 0) {
			ConsolidatedException causes = new ConsolidatedException(Msg.bind(
					Messages.DUNIDDocEx_FOUND_DUNID_RESUME, DUNID_ATTR));
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new NodeRelatedException(node, Msg.bind(
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
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             {@inheritDoc}
	 * @throws IllegalDirectoryException
	 *             {@inheritDoc}
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
	 */
	public Element getElement(DUNID dunid) {
		return DUNIDDocHelper.getElement(getDocument(), dunid);
	}

}
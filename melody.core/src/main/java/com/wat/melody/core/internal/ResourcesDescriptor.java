package com.wat.melody.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.MutationEvent;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.Messages;
import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetsFilterException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.DUNIDDoc;
import com.wat.melody.common.xml.DUNIDDocHelper;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesDescriptor extends FilteredDoc implements
		IResourcesDescriptor {

	private TargetsDescriptor _targetsDescriptor;
	private List<DUNIDDoc> _DUNIDDocList;

	/**
	 * <p>
	 * Build an empty {@link ResourcesDescriptor} and initialize all members to
	 * their default values.
	 * </p>
	 * <p>
	 * A {@link ResourcesDescriptor} is useful to :<BR/>
	 * - reduce the content of the underlying XML to a subset (e.g. filtering :
	 * using XPath 2.0 expression, only matching node will be eligible to query)
	 * ; <BR/>
	 * - query the content of the underlying XML (e.g. evaluate: based on XPath
	 * 2.0 expression, content can be selected) ; <BR/>
	 * </p>
	 */
	public ResourcesDescriptor() {
		super();
		try {
			loadFromXML("<resources/>");
		} catch (IllegalDocException | IllegalFilterException | IOException Ex) {
			throw new RuntimeException("Unexecpted error while initializing "
					+ "the " + ResourcesDescriptor.class.getSimpleName() + ". "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		// Build a new targets descriptor
		setTargetsDescriptor(new TargetsDescriptor());
		// Load it with this object
		try {
			getTargetsDescriptor().load(this);
		} catch (IllegalTargetsFilterException Ex) {
			throw new RuntimeException("Unexecpted error while initializing "
					+ "the " + TargetsDescriptor.class.getSimpleName() + ". "
					+ "Because no Filters have been defined yet, such error "
					+ "cannot happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		// Build the list of DUNIDoc
		setDUNIDDocList(new ArrayList<DUNIDDoc>());
	}

	private TargetsDescriptor getTargetsDescriptor() {
		return _targetsDescriptor;
	}

	private TargetsDescriptor setTargetsDescriptor(TargetsDescriptor td) {
		if (td == null) {
			throw new IllegalArgumentException("nul: Not accepted. "
					+ "Must be a valid "
					+ TargetsDescriptor.class.getCanonicalName() + ".");
		}
		TargetsDescriptor previous = getTargetsDescriptor();
		_targetsDescriptor = td;
		return previous;
	}

	private List<DUNIDDoc> getDUNIDDocList() {
		return _DUNIDDocList;
	}

	private List<DUNIDDoc> setDUNIDDocList(List<DUNIDDoc> srds) {
		if (srds == null) {
			throw new IllegalArgumentException("nul: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ DUNIDDoc.class.getCanonicalName() + ">.");
		}
		List<DUNIDDoc> previous = getDUNIDDocList();
		_DUNIDDocList = srds;
		return previous;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("resources-descriptors:");
		str.append(getDUNIDDocList());
		str.append(", resources-filters:");
		str.append(getFilterSet());
		str.append(", target-filter:");
		str.append(getTargetsDescriptor().getFilterSet());
		str.append(" }");
		return str.toString();
	}

	@Override
	public String fulldump() {
		StringBuilder str = new StringBuilder();
		str.append(super.fulldump().replaceAll(SysTool.NEW_LINE,
				SysTool.NEW_LINE + "  "));
		str.append(SysTool.NEW_LINE);
		str.append(SysTool.NEW_LINE);
		str.append(getTargetsDescriptor().fulldump().replaceAll(
				SysTool.NEW_LINE, SysTool.NEW_LINE + "  "));
		str.append(SysTool.NEW_LINE);
		str.append(SysTool.NEW_LINE);
		for (DUNIDDoc doc : getDUNIDDocList()) {
			str.append(doc.fulldump().replaceAll(SysTool.NEW_LINE,
					SysTool.NEW_LINE + "  "));
			str.append(SysTool.NEW_LINE);
			str.append(SysTool.NEW_LINE);
		}
		return str.toString();
	}

	@Override
	public XPath setXPath(XPath xpath) {
		XPath previous = super.setXPath(xpath);
		if (getTargetsDescriptor() != null) {
			getTargetsDescriptor().setXPath(xpath);
		}
		return previous;
	}

	@Override
	public DUNID getMelodyID(Element n) {
		return DUNIDDocHelper.getDUNID(n);
	}

	@Override
	public List<Element> evaluateTargets(String xpath)
			throws XPathExpressionException {
		List<Element> targets = new ArrayList<Element>();
		// Evaluate expression in the current document
		synchronized (getDocument()) {
			NodeList nl = evaluateAsNodeList(xpath);
			// Search for resulting nodes in the eligible targets
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) {
					throw new XPathExpressionException(Messages.bind(
							Messages.TargetEx_NOT_MATCH_ELEMENT, xpath,
							DocHelper.parseNodeType(nl.item(i))));
				}
				Element n = (Element) nl.item(i);
				if (getTargetsDescriptor().getElement(
						DUNIDDocHelper.getDUNID(n)) != null) {
					targets.add(n);
				}
			}
		}
		return targets;
	}

	/**
	 * @param sPath
	 *            is the path of the {@link DUNIDDoc} to search. Can be
	 *            <tt>null</tt>.
	 * 
	 * @return the {@link DUNIDDoc} which have been loaded from the given file,
	 *         or <tt>null</tt> if the given path is <tt>null</tt>.
	 */
	private DUNIDDoc findDUNIDDoc(String sPath) {
		Integer i = findDUNIDDocIndex(sPath);
		return i == null ? null : getDUNIDDocList().get(i);
	}

	/**
	 * @param sPath
	 *            is the path of the {@link DUNIDDoc} to search. Can be
	 *            <tt>null</tt>.
	 * 
	 * @return the index of the {@link DUNIDDoc} within this object's
	 *         {@link DUNIDDoc} list, which have been loaded from the given
	 *         file, or <tt>null</tt> if the given path is <tt>null</tt>.
	 */
	private Integer findDUNIDDocIndex(String sPath) {
		for (int i = 0; i < getDUNIDDocList().size(); i++) {
			if (getDUNIDDocList().get(i).getSourceFile().equals(sPath)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * @param e
	 *            is an {@link Element} Node.
	 * 
	 * @return the {@link DUNIDDoc} which contains the given {@link Element}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	private DUNIDDoc getOwnerDUNIDDoc(Element e) {
		if (e == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		String sourcefile = DocHelper.getNodeLocation(e).getSource();
		DUNIDDoc found = findDUNIDDoc(sourcefile);
		if (found != null) {
			return found;
		}
		throw new RuntimeException("Unexecpted error while searching a "
				+ "DUNIDDoc." + "'" + sourcefile
				+ "' doesn't match any DUNIDDoc. "
				+ "Source code has certainly been modified and "
				+ "a bug have been introduced.");
	}

	/**
	 * @param sPath
	 *            is the path of an xml file to add to this object's resources.
	 * 
	 * @return <tt>true</tt> if the given xml file have been successfully added
	 *         to this object's resources.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't point to a valid file.
	 * @throws IllegalDocException
	 *             if the given file is not valid (not a xml file, invalid
	 *             content).
	 * @throws IllegalResourcesFilterException
	 *             if a {@link Filter} is not valid.
	 * @throws IllegalTargetsFilterException
	 *             if a {@link Filter} is not valid.
	 */
	@Override
	public synchronized boolean add(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalTargetsFilterException,
			IllegalResourcesFilterException, IOException {
		if (findDUNIDDoc(sPath) != null) {
			return false;
		}
		try {
			// Add in the list
			DUNIDDoc d = new DUNIDDoc();
			d.setXPath(getXPath());
			d.load(sPath);
			getDUNIDDocList().add(d);
			// Add the content
			Node n = getOriginalDocument().importNode(
					d.getDocument().getFirstChild(), true);
			stopListening();
			getOriginalDocument().getFirstChild().appendChild(n);
			startListening();
			// Rebuild
			restoreOriginalDocument();
			validateHeritAttrs();
			applyFilters();
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		} catch (IllegalDocException | IllegalFileException Ex) {
			throw Ex;
		} catch (MelodyException Ex) {
			throw new RuntimeException("Unexecpted error while loading "
					+ "a DUNIDDoc. "
					+ "Because MelodyException cannot be raise by the "
					+ "underlying Doc, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		// Update targets descriptor
		getTargetsDescriptor().load(this);
		return true;
	}

	/**
	 * @param sPath
	 *            is the path of an xml file to remove from this object's
	 *            resources.
	 * 
	 * @return <tt>true</tt> if the given xml file have been successfully
	 *         removed from this object's resources.
	 * 
	 * @throws IllegalDocException
	 *             if this object's resources are no more valid after removal
	 *             (invalid content).
	 * @throws IllegalResourcesFilterException
	 *             if a {@link Filter} is not valid.
	 * @throws IllegalTargetsFilterException
	 *             if a {@link Filter} is not valid.
	 */
	@Override
	public synchronized boolean remove(String sPath)
			throws IllegalDocException, IllegalResourcesFilterException,
			IllegalTargetsFilterException {
		Integer i = findDUNIDDocIndex(sPath);
		if (i == null) {
			return false;
		}
		// Remove from list
		getDUNIDDocList().remove(i);
		// Remove the content
		Node base = getOriginalDocument().getFirstChild();
		stopListening();
		base.removeChild(base.getChildNodes().item(i));
		startListening();
		try {
			// Rebuild
			restoreOriginalDocument();
			validateHeritAttrs();
			applyFilters();
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		} catch (IllegalDocException Ex) {
			throw Ex;
		}
		// Update targets descriptor
		getTargetsDescriptor().load(this);
		return true;
	}

	/**
	 * <p>
	 * Store all this object's resources (see {@link #add(String)}).
	 * </p>
	 */
	@Override
	public synchronized void store() {
		for (DUNIDDoc d : getDUNIDDocList()) {
			d.store();
		}
	}

	/**
	 * <p>
	 * Due to the nature of this object, this operation is not supported.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *             every time.
	 */
	public void store(String path) {
		throw new RuntimeException("It is not possible to store a "
				+ ResourcesDescriptor.class.getCanonicalName()
				+ " at a custom location.");
	}

	@Override
	public String evaluateAsString(String expr) throws XPathExpressionException {
		return XPathExpander.evaluateAsString(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public NodeList evaluateAsNodeList(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNodeList(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public Node evaluateAsNode(String expr) throws XPathExpressionException {
		return XPathExpander
				.evaluateAsNode(expr, getDocument().getFirstChild());
	}

	@Override
	protected String getSmartMsg() {
		return "resources-descriptor";
	}

	/**
	 * An element node have been inserted in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void elementInstered(MutationEvent evt) throws MelodyException {
		super.elementInstered(evt);
		// the inserted node
		Element t = (Element) evt.getTarget();
		// its next sibling
		Node s = t.getNextSibling();
		while (s != null && s.getNodeType() != Node.ELEMENT_NODE) {
			s = s.getNextSibling();
		}
		DUNID sdunid = DUNIDDocHelper.getDUNID((Element) s);
		// its parent node
		Element p = (Element) t.getParentNode();
		DUNID pdunid = DUNIDDocHelper.getDUNID(p);
		// Modify the targets descriptor
		Document d = getTargetsDescriptor().getDocument();
		Element pori = DUNIDDocHelper.getElement(d, pdunid);
		if (pori != null) { // inserted node parent is in the targets descriptor
			pori.insertBefore(d.importNode(t, true),
					DUNIDDocHelper.getElement(d, sdunid));
		}
		// Modify the DUNIDDoc
		d = getOwnerDUNIDDoc(p).getDocument();
		pori = DUNIDDocHelper.getElement(d, pdunid);
		pori.insertBefore(d.importNode(t, true),
				DUNIDDocHelper.getElement(d, sdunid));
	}

	/**
	 * An element node have been removed in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void elementRemoved(MutationEvent evt) throws MelodyException {
		super.elementRemoved(evt);
		// the removed node
		Element t = (Element) evt.getTarget();
		DUNID tdunid = DUNIDDocHelper.getDUNID(t);
		// its parent node
		Element p = (Element) t.getParentNode();
		DUNID pdunid = DUNIDDocHelper.getDUNID(p);
		// Modify the targets descriptor
		Document d = getTargetsDescriptor().getDocument();
		Element tori = DUNIDDocHelper.getElement(d, tdunid);
		if (tori != null) { // removed node is in the targets descriptor
			DUNIDDocHelper.getElement(d, pdunid).removeChild(tori);
		}
		// Modify the DUNIDDoc
		d = getOwnerDUNIDDoc(p).getDocument();
		tori = DUNIDDocHelper.getElement(d, tdunid);
		DUNIDDocHelper.getElement(d, pdunid).removeChild(tori);
	}

	/**
	 * A leaf text node have been inserted in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void textLeafInserted(MutationEvent evt) throws MelodyException {
		super.textLeafInserted(evt);
		// the changed node
		Text t = (Text) evt.getTarget();
		// its parent element
		Element e = (Element) t.getParentNode();
		DUNID edunid = DUNIDDocHelper.getDUNID(e);
		// Modify the targets descriptor
		Element eori = getTargetsDescriptor().getElement(edunid);
		if (eori != null) { // changed node is in the targets descriptor
			// It is assume that the Element is a leaf, so setTextContent is OK
			eori.setTextContent(t.getTextContent());
		}
		// Modify the DUNIDDoc
		eori = getOwnerDUNIDDoc(e).getElement(edunid);
		// It is assume that the Element is a leaf, so setTextContent is OK
		eori.setTextContent(t.getTextContent());
	}

	/**
	 * A leaf text node have been removed in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void textLeafRemoved(MutationEvent evt) throws MelodyException {
		super.textLeafRemoved(evt);
		// the changed node
		Text t = (Text) evt.getTarget();
		// its parent element
		Element e = (Element) t.getParentNode();
		DUNID edunid = DUNIDDocHelper.getDUNID(e);
		// Modify the targets descriptor
		Element eori = getTargetsDescriptor().getElement(edunid);
		if (eori != null) { // changed node is in the targets descriptor
			// It is assume that the Element is a leaf, so getFirstChild is OK
			eori.removeChild(eori.getFirstChild());
		}
		// Modify the DUNIDDoc
		eori = getOwnerDUNIDDoc(e).getElement(edunid);
		// It is assume that the Element is a leaf, so getFirstChild is OK
		eori.removeChild(eori.getFirstChild());
	}

	/**
	 * The content of a leaf text node have been modified in the current
	 * document => modify the original DUNIDDoc and the targets descriptor
	 */
	protected void textLeafModified(MutationEvent evt) throws MelodyException {
		super.textLeafModified(evt);
		// the changed node
		Text t = (Text) evt.getTarget();
		// its parent element
		Element e = (Element) t.getParentNode();
		DUNID edunid = DUNIDDocHelper.getDUNID(e);
		// Modify the targets descriptor
		Element eori = getTargetsDescriptor().getElement(edunid);
		if (eori != null) { // changed node is in the targets descriptor
			// It is assume that the Element is a leaf, so getFirstChild is OK
			eori.getFirstChild().setNodeValue(t.getTextContent());
		}
		// Modify the DUNIDDoc
		eori = getOwnerDUNIDDoc(e).getElement(edunid);
		// It is assume that the Element is a leaf, so getFirstChild is OK
		eori.getFirstChild().setNodeValue(t.getTextContent());
	}

	/**
	 * An attribute have been inserted in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void attributeInserted(MutationEvent evt) throws MelodyException {
		super.attributeInserted(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID tdunid = DUNIDDocHelper.getDUNID(t);
		// Modify the targets descriptor
		Element tori = getTargetsDescriptor().getElement(tdunid);
		if (tori != null) { // target node is in the targets descriptor
			tori.setAttribute(evt.getAttrName(), evt.getNewValue());
		}
		// Modify the DUNIDDoc
		tori = getOwnerDUNIDDoc(t).getElement(tdunid);
		tori.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	/**
	 * An attribute have been removed in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void attributeRemoved(MutationEvent evt) throws MelodyException {
		super.attributeRemoved(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID tdunid = DUNIDDocHelper.getDUNID(t);
		// Modify the targets descriptor
		Element tori = getTargetsDescriptor().getElement(tdunid);
		if (tori != null) { // target node is in the targets descriptor
			tori.removeAttribute(evt.getAttrName());
		}
		// Modify the DUNIDDoc
		tori = getOwnerDUNIDDoc(t).getElement(tdunid);
		tori.removeAttribute(evt.getAttrName());
	}

	/**
	 * An attribute have been modified in the current document => modify the
	 * original DUNIDDoc and the targets descriptor
	 */
	protected void attributeModified(MutationEvent evt) throws MelodyException {
		super.attributeModified(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID tdunid = DUNIDDocHelper.getDUNID(t);
		// Modify the targets descriptor
		Element tori = getTargetsDescriptor().getElement(tdunid);
		if (tori != null) { // target node is in the targets descriptor
			tori.setAttribute(evt.getAttrName(), evt.getNewValue());
		}
		// Modify the DUNIDDoc
		tori = getOwnerDUNIDDoc(t).getElement(tdunid);
		tori.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	@Override
	public synchronized Filter removeFilter(int i) {
		Filter sRemovedFilter = super.removeFilter(i);
		try {
			getTargetsDescriptor().load(this);
		} catch (IllegalTargetsFilterException Ex) {
			throw new RuntimeException("Unexecpted error in Target "
					+ "Descriptor while removing a filter. "
					+ "Because all filters have already been removed, "
					+ "it is impossible to find a invalid filter here"
					+ "Source code has certainly been modified "
					+ "and a bug have been introduced.", Ex);
		}
		return sRemovedFilter;
	}

	@Override
	public synchronized void clearFilters() {
		super.clearFilters();
		try {
			getTargetsDescriptor().load(this);
		} catch (IllegalTargetsFilterException Ex) {
			throw new RuntimeException("Unexecpted error in Target "
					+ "Descriptor while clearing all Resources Filters. "
					+ "Because there are no more filters, it is impossible "
					+ "to find a invalid filter here"
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	@Override
	public synchronized Filter setFilter(int i, Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException {
		Filter sRemovedFilter;
		try {
			sRemovedFilter = super.setFilter(i, filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetsDescriptor().load(this);
		return sRemovedFilter;
	}

	@Override
	public synchronized void setFilterSet(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException {
		try {
			super.setFilterSet(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetsDescriptor().load(this);
	}

	@Override
	public synchronized void addFilter(Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException {
		try {
			super.addFilter(filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetsDescriptor().load(this);
	}

	@Override
	public synchronized void addFilters(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException {
		try {
			super.addFilters(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetsDescriptor().load(this);
	}

	@Override
	public FilterSet getTargetFilterSet() {
		return getTargetsDescriptor().getFilterSet();
	}

	@Override
	public synchronized int countTargetFilters() {
		return getTargetsDescriptor().countFilters();
	}

	@Override
	public synchronized Filter getTargetFilter(int i) {
		return getTargetsDescriptor().getFilter(i);
	}

	@Override
	public synchronized Filter removeTargetFilter(int i) {
		return getTargetsDescriptor().removeFilter(i);
	}

	@Override
	public synchronized void clearTargetFilters() {
		getTargetsDescriptor().clearFilters();
	}

	@Override
	public synchronized Filter setTargetFilter(int i, Filter filter)
			throws IllegalTargetsFilterException {
		return getTargetsDescriptor().setFilter(i, filter);
	}

	@Override
	public synchronized void setTargetFilterSet(FilterSet filters)
			throws IllegalTargetsFilterException {
		getTargetsDescriptor().setFilterSet(filters);
	}

	@Override
	public synchronized void addTargetFilter(Filter filter)
			throws IllegalTargetsFilterException {
		getTargetsDescriptor().addFilter(filter);
	}

	@Override
	public synchronized void addTargetFilters(FilterSet filters)
			throws IllegalTargetsFilterException {
		getTargetsDescriptor().addFilters(filters);
	}

}
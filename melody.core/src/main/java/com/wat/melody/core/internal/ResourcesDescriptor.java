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
import com.wat.melody.api.exception.IllegalTargetFilterException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
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
		// Build a new empty OriginalDocument
		setOriginalDocument(DocHelper.newDocument());
		// Add a first XML Element called root
		Element root = getOriginalDocument().createElement("resources");
		getOriginalDocument().appendChild(root);
		// Add it an DUNIND XML attribute
		root.setAttribute(DUNID_ATTR, new DUNID().getValue());
		// The Current Document is a clone of the Original Document
		setDocument(cloneOriginalDocument());
		// Build a new targets descriptor
		setTargetsDescriptor(new TargetsDescriptor());
		// Load it with this object
		try {
			getTargetsDescriptor().load(this);
		} catch (IllegalTargetFilterException Ex) {
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
					+ "Must be a valid List<DUNIDDoc>.");
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
	public XPath setXPath(XPath xpath) {
		XPath previous = super.setXPath(xpath);
		if (getTargetsDescriptor() != null) {
			getTargetsDescriptor().setXPath(xpath);
		}
		return previous;
	}

	@Override
	public synchronized DUNID getMelodyID(Element n) {
		return DUNIDDocHelper.getDUNID(n);
	}

	@Override
	public synchronized List<Element> evaluateTargets(String xpath)
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

	private DUNIDDoc findDUNIDDoc(String sPath) {
		for (DUNIDDoc d : getDUNIDDocList()) {
			if (d.getSourceFile().equals(sPath)) {
				return d;
			}
		}
		return null;
	}

	private DUNIDDoc getOwnerDUNIDDoc(Element e) {
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

	@Override
	public synchronized boolean add(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalTargetFilterException,
			IllegalResourcesFilterException, IOException {
		try {
			if (sPath == null) {
				return false;
			}
			if (findDUNIDDoc(sPath) != null) {
				return false;
			}
			// Add in the list
			DUNIDDoc d = new DUNIDDoc();
			d.setXPath(getXPath());
			d.load(sPath);
			getDUNIDDocList().add(d);
			// Add the content
			Node n = getOriginalDocument().importNode(
					d.getDocument().getFirstChild(), true);
			getOriginalDocument().getFirstChild().appendChild(n);
			// Rebuild
			setDocument(cloneOriginalDocument());
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

	@Override
	public synchronized boolean remove(String sPath)
			throws IllegalDocException, IllegalResourcesFilterException,
			IllegalTargetFilterException {
		if (sPath == null) {
			return false;
		}
		int i = -1;
		// Search the doc in the list
		while (++i < getDUNIDDocList().size()) {
			DUNIDDoc d = getDUNIDDocList().get(i);
			if (d.getSourceFile().equals(sPath)) {
				break;
			}
		}
		if (i > getDUNIDDocList().size()) {
			// Not found
			return false;
		}
		// Remove from list
		getDUNIDDocList().remove(i);
		// Remove the content
		Node base = getOriginalDocument().getFirstChild();
		base.removeChild(base.getChildNodes().item(i));
		try {
			// Rebuild
			setDocument(cloneOriginalDocument());
			validateHeritAttrs();
			applyFilters();
		} catch (IllegalDocException Ex) {
			throw Ex;
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		// Update targets descriptor
		getTargetsDescriptor().load(this);
		return true;
	}

	/**
	 * <p>
	 * Store all {@link Document} this object contains (see {@link #add(String)}
	 * ).
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
	 *             all time.
	 */
	public synchronized void store(String path) {
		throw new RuntimeException("It is not possible to store a "
				+ ResourcesDescriptor.class.getCanonicalName()
				+ " at a custom location.");
	}

	@Override
	public synchronized String evaluateAsString(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsString(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public synchronized NodeList evaluateAsNodeList(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNodeList(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public synchronized Node evaluateAsNode(String expr)
			throws XPathExpressionException {
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
	protected void nodeInstered(MutationEvent evt) throws MelodyException {
		super.nodeInstered(evt);
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
	protected void nodeRemoved(MutationEvent evt) throws MelodyException {
		super.nodeRemoved(evt);
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
	 * The text of an element node have been changed in the current document =>
	 * modify the original DUNIDDoc and the targets descriptor
	 */
	protected void nodeTextChanged(MutationEvent evt) throws MelodyException {
		super.nodeTextChanged(evt);
		// the changed node
		Text t = (Text) evt.getTarget();
		// its parent element
		Element e = (Element) t.getParentNode();
		DUNID edunid = DUNIDDocHelper.getDUNID(e);
		// Modify the targets descriptor
		Element eori = getTargetsDescriptor().getElement(edunid);
		if (eori != null) { // changed node is in the targets descriptor
			eori.setTextContent(t.getTextContent());
		}
		// Modify the DUNIDDoc
		eori = getOwnerDUNIDDoc(e).getElement(edunid);
		eori.setTextContent(t.getTextContent());
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
		} catch (IllegalTargetFilterException Ex) {
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
		} catch (IllegalTargetFilterException Ex) {
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
			IllegalTargetFilterException {
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
			IllegalTargetFilterException {
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
			IllegalTargetFilterException {
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
			IllegalTargetFilterException {
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
			throws IllegalTargetFilterException {
		return getTargetsDescriptor().setFilter(i, filter);
	}

	@Override
	public synchronized void setTargetFilterSet(FilterSet filters)
			throws IllegalTargetFilterException {
		getTargetsDescriptor().setFilterSet(filters);
	}

	@Override
	public synchronized void addTargetFilter(Filter filter)
			throws IllegalTargetFilterException {
		getTargetsDescriptor().addFilter(filter);
	}

	@Override
	public synchronized void addTargetFilters(FilterSet filters)
			throws IllegalTargetFilterException {
		getTargetsDescriptor().addFilters(filters);
	}

}
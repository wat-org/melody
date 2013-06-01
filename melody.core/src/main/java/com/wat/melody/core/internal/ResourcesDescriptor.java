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
import com.wat.melody.common.xml.Doc;
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

	private TargetDescriptor _targetDescriptor;
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
		setOriginalDocument(newDocument());
		// Add a first XML Element called root
		Element root = getOriginalDocument().createElement("resources");
		getOriginalDocument().appendChild(root);
		// Add it an DUNIND XML attribute
		root.setAttribute(DUNID_ATTR, new DUNID(0).getValue());
		// The Current Document is a clone of the Original Document
		setDocument(cloneOriginalDocument());
		// Build a new TargetDescriptor
		setTargetDescriptor(new TargetDescriptor());
		// Load it with this object
		try {
			getTargetDescriptor().load(this);
		} catch (IllegalTargetFilterException Ex) {
			throw new RuntimeException("Unexecpted error while initializing "
					+ "the " + TargetDescriptor.class.getSimpleName() + ". "
					+ "Because no Filters have been defined yet, such error "
					+ "cannot happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		// Build the list of DUNIDoc
		setDUNIDDocList(new ArrayList<DUNIDDoc>());
	}

	private TargetDescriptor getTargetDescriptor() {
		return _targetDescriptor;
	}

	private TargetDescriptor setTargetDescriptor(TargetDescriptor td) {
		if (td == null) {
			throw new IllegalArgumentException("nul: Not accepted. "
					+ "Must be a valid "
					+ TargetDescriptor.class.getCanonicalName() + ".");
		}
		TargetDescriptor previous = getTargetDescriptor();
		_targetDescriptor = td;
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
		str.append(getTargetDescriptor().getFilterSet());
		str.append(" }");
		return str.toString();
	}

	@Override
	public XPath setXPath(XPath xpath) {
		XPath previous = super.setXPath(xpath);
		getTargetDescriptor().setXPath(xpath);
		return previous;
	}

	@Override
	public synchronized DUNID getMelodyID(Element n) {
		return getDUNID(n);
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
							Doc.parseNodeType(nl.item(i))));
				}
				Element n = (Element) nl.item(i);
				if (getTargetDescriptor().getElement(getDUNID(n)) != null) {
					targets.add(n);
				}
			}
		}
		return targets;
	}

	private int getNextDUNIDDocIndex() {
		if (getDUNIDDocList().size() == 0) {
			return 1;
		}
		return getDUNIDDocList().get(getDUNIDDocList().size() - 1).getIndex() + 1;
	}

	private DUNIDDoc getOwnerDUNIDDoc(DUNID dunid) {
		int did = dunid.getDID();
		for (DUNIDDoc d : getDUNIDDocList()) {
			if (d.getIndex() == did) {
				return d;
			}
		}
		throw new RuntimeException("Unexecpted error while searching a "
				+ "DUNIDDoc." + "No DUNIDDoc match the given DID. "
				+ "Source code has certainly been modified and "
				+ "a bug have been introduced.");
	}

	@Override
	public synchronized void add(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalTargetFilterException,
			IllegalResourcesFilterException, IOException {
		try {
			// Add in the list
			DUNIDDoc d = new DUNIDDoc(getNextDUNIDDocIndex());
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
		// Update targetDesc
		getTargetDescriptor().load(this);
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
		// Update targetDesc
		getTargetDescriptor().load(this);
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

	protected void nodeInstered(MutationEvent evt) throws MelodyException {
		super.nodeInstered(evt);
		// TODO : Modify the DUNIDDoc and the target descriptor
	}

	protected void nodeRemoved(MutationEvent evt) throws MelodyException {
		super.nodeRemoved(evt);
		// TODO : Modify the DUNIDDoc and the target descriptor
	}

	protected void nodeTextChanged(MutationEvent evt) throws MelodyException {
		super.nodeTextChanged(evt);
		// TODO : Modify the DUNIDDoc and the target descriptor
	}

	/**
	 * An attribute have been inserted in the current document => modify the
	 * original DUNIDDoc and the target descriptor
	 */
	protected void attributeInserted(MutationEvent evt) throws MelodyException {
		super.attributeInserted(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		// Modify the DUNIDDoc
		Element n = getOwnerDUNIDDoc(dunid).getElement(dunid);
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
		// Modify the target descriptor
		n = getTargetDescriptor().getElement(dunid);
		if (n == null) {
			// This DUNID may not be in the target descriptor
			return;
		}
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	/**
	 * An attribute have been removed in the current document => modify the
	 * original DUNIDDoc and the target descriptor
	 */
	protected void attributeRemoved(MutationEvent evt) throws MelodyException {
		super.attributeRemoved(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		// Modify the DUNIDDoc
		Element n = getOwnerDUNIDDoc(dunid).getElement(dunid);
		n.removeAttribute(evt.getAttrName());
		// Modify the target descriptor
		n = getTargetDescriptor().getElement(dunid);
		if (n == null) {
			// This DUNID may not be in the target descriptor
			return;
		}
		n.removeAttribute(evt.getAttrName());
	}

	/**
	 * An attribute have been modified in the current document => modify the
	 * original DUNIDDoc and the target descriptor
	 */
	protected void attributeModified(MutationEvent evt) throws MelodyException {
		super.attributeModified(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		// Modify the DUNIDDoc
		Element n = getOwnerDUNIDDoc(dunid).getElement(dunid);
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
		// Modify the target descriptor
		n = getTargetDescriptor().getElement(dunid);
		if (n == null) {
			// This DUNID may not be in the target descriptor
			return;
		}
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	@Override
	public synchronized String removeFilter(int i) {
		String sRemovedFilter = super.removeFilter(i);
		try {
			getTargetDescriptor().load(this);
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
			getTargetDescriptor().load(this);
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
	public synchronized String setFilter(int i, Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException {
		String sRemovedFilter;
		try {
			sRemovedFilter = super.setFilter(i, filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetDescriptor().load(this);
		return sRemovedFilter;
	}

	@Override
	public synchronized void setFilters(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException {
		try {
			super.setFilters(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		getTargetDescriptor().load(this);
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
		getTargetDescriptor().load(this);
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
		getTargetDescriptor().load(this);
	}

	@Override
	public synchronized int countTargetsFilters() {
		return getTargetDescriptor().countFilters();
	}

	@Override
	public synchronized String getTargetsFilter(int i) {
		return getTargetDescriptor().getFilter(i);
	}

	@Override
	public synchronized String removeTargetsFilter(int i) {
		return getTargetDescriptor().removeFilter(i);
	}

	@Override
	public synchronized void clearTargetsFilters() {
		getTargetDescriptor().clearFilters();
	}

	@Override
	public synchronized String setTargetsFilter(int i, Filter filter)
			throws IllegalTargetFilterException {
		return getTargetDescriptor().setFilter(i, filter);
	}

	@Override
	public synchronized void setTargetsFilters(FilterSet filters)
			throws IllegalTargetFilterException {
		getTargetDescriptor().setFilters(filters);
	}

	@Override
	public synchronized void addTargetsFilter(Filter filter)
			throws IllegalTargetFilterException {
		getTargetDescriptor().addFilter(filter);
	}

	@Override
	public synchronized void addTargetsFilters(FilterSet filters)
			throws IllegalTargetFilterException {
		getTargetDescriptor().addFilters(filters);
	}

}
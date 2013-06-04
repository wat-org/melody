package com.wat.melody.common.xml;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * <p>
 * A {@link FilteredDoc} holds a {@link Document}. It exposes methods to select
 * a subset of this {@link Document}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDoc extends DUNIDDoc {

	/*
	 * TODO : should remove all text Node, in order to free memory and enhance
	 * xpath queries performance
	 */
	/**
	 * XML attribute of an XML Element, which reference an other XML Element.
	 */
	public static final String HERIT_ATTR = "herit";

	private Document _originalDoc = null;
	private FilterSet _filters;

	public FilteredDoc() {
		super();
		setFilters(new FilterSet());
	}

	/**
	 * @return the {@link Document} used to load this object, where no
	 *         {@link Filter} apply.
	 */
	protected Document getOriginalDocument() {
		return _originalDoc;
	}

	protected Document setOriginalDocument(Document doc) {
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		Document previous = getOriginalDocument();
		_originalDoc = doc;
		return previous;
	}

	/**
	 * <p>
	 * In order to make this object consistent, {@link #applyFilters()} must be
	 * called after every modification of the returned object.
	 * </p>
	 * 
	 * @return this object's {@link FilterSet}.
	 */
	private synchronized FilterSet getFilters() {
		return _filters;
	}

	private synchronized FilterSet setFilters(FilterSet filters) {
		if (filters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FilterSet.class.getCanonicalName()
					+ ".");
		}
		FilterSet previous = getFilters();
		_filters = filters;
		return previous;
	}

	/**
	 * <p>
	 * Restore this object's original {@link Document}.
	 * </p>
	 */
	protected void restoreOriginalDocument() {
		if (getOriginalDocument() != null) {
			setDocument((Document) getOriginalDocument().cloneNode(true));
		}
	}

	/**
	 * <p>
	 * Load the content of the xml file points by the given path into this
	 * object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will apply filters ;</li>
	 * </ul>
	 * 
	 * @param sPath
	 * 
	 * @throws IllegalDocException
	 *             {@inheritDoc}
	 * @throws IllegalFileException
	 *             {@inheritDoc}
	 * @throws IllegalFilterException
	 *             {@inheritDoc}
	 * @throws IOException
	 *             {@inheritDoc}
	 */
	@Override
	public synchronized void load(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalFilterException, IOException {
		try {
			super.load(sPath);
		} catch (IllegalDocException | IllegalFileException | IOException Ex) {
			throw Ex;
		} catch (MelodyException Ex) {
			throw new RuntimeException("Unexecpted error while loading "
					+ "a FilteredDoc. "
					+ "Because MelodyException cannot be raise by the "
					+ "underlying Doc, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		setOriginalDocument((Document) getDocument().cloneNode(true));
		applyFilters();
	}

	/**
	 * <p>
	 * Load the given {@link DUNIDDoc} into this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will apply filters ;</li>
	 * <li>Further modification of this object doesn't affect the given
	 * {@link DUNIDDoc} ;</li>
	 * </ul>
	 * 
	 * @param doc
	 * 
	 * @throws IllegalFilterException
	 *             {@inheritDoc}
	 */
	public synchronized void load(DUNIDDoc doc) throws IllegalFilterException {
		super.load(doc);
		setOriginalDocument((Document) getDocument().cloneNode(true));
		applyFilters();
	}

	/**
	 * <p>
	 * Load the given XML content into this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will also apply filters ;</li>
	 * </ul>
	 * 
	 * @param doc
	 * 
	 * @throws IllegalFilterException
	 *             {@inheritDoc}
	 * @throws IllegalDocException
	 *             {@inheritDoc}
	 * @throws IOException
	 *             {@inheritDoc}
	 */
	public synchronized void loadFromXML(String xml)
			throws IllegalDocException, IllegalFilterException, IOException {
		try {
			super.loadFromXML(xml);
		} catch (MelodyException Ex) {
			throw new RuntimeException("Unexecpted error while loading "
					+ "a FilteredDoc. "
					+ "Because MelodyException cannot be raise by the "
					+ "underlying Doc, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		setOriginalDocument((Document) getDocument().cloneNode(true));
		applyFilters();
	}

	/**
	 * @throws IllegalDocException
	 *             if one or more {@link Element}s have an invalid
	 *             {@link #HERIT_ATTR} XML Attribute (match no nodes, match
	 *             multiple node, doesn't contains a valid xpath expression,
	 *             circular ref). {@link #HERIT_ATTR} XML Attribute is a
	 *             reserved attribute, which allow to define heritage between
	 *             {@link Element}s.
	 * @throws IllegalDocException
	 *             {@inheritDoc}
	 */
	@Override
	protected synchronized void validateContent() throws IllegalDocException {
		super.validateContent();
		validateHeritAttrs();
	}

	/**
	 * @throws IllegalDocException
	 *             if one or more {@link Element}s have an invalid
	 *             {@link #HERIT_ATTR} XML Attribute (match no nodes, match
	 *             multiple node, doesn't contains a valid xpath expression,
	 *             circular ref). {@link #HERIT_ATTR} XML Attribute is a
	 *             reserved attribute, which allow to define heritage between
	 *             {@link Element}s.
	 */
	public synchronized void validateHeritAttrs() throws IllegalDocException {
		try {
			FilteredDocHelper.validateParentHeritedNodes(getDocument());
		} catch (NodeRelatedException Ex) {
			throw new IllegalDocException(Ex);
		}
	}

	public synchronized void store(String sPath) throws IllegalFileException,
			IllegalDirectoryException {
		Document memory = getDocument();
		setDocument(getOriginalDocument());
		super.store(sPath);
		setDocument(memory);
	}

	/**
	 * @return a shallow copy of this object's {@link FilterSet} (The elements
	 *         themselves are not copied; If the returned {@link FilterSet} is
	 *         modified, this object's {@link FilterSet} will not be modified).
	 */
	public synchronized FilterSet getFilterSet() {
		return (FilterSet) _filters.clone();
	}

	/**
	 * @param i
	 *            is the position in this object's {@link FilterSet} of the
	 *            requested {@link Filter}.
	 * 
	 * @return the {@link Filter} which is located at the given position.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the given position is out of this object's
	 *             {@link FilterSet}'s range.
	 */
	public synchronized Filter getFilter(int i) {
		return _filters.get(i);
	}

	/**
	 * @param filters
	 *            is a {@link FilterSet} to add to this object's
	 *            {@link FilterSet}.
	 * 
	 * @throws IllegalFilterException
	 *             if one {@link Filter} is already included in this object's
	 *             {@link FilterSet}.
	 * @throws IllegalFilterException
	 *             if one {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if one {@link Filter} doesn't match any {@link Node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link FilterSet} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if one {@link FilterSet} is <tt>null</tt>.
	 */
	public synchronized void addFilters(FilterSet filters)
			throws IllegalFilterException {
		if (filters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FilterSet.class.getCanonicalName()
					+ ".");
		}
		for (Filter filter : filters) {
			addFilter(filter);
		}
	}

	/**
	 * @param filter
	 *            is an XPath Expression to add to this object's
	 *            {@link FilterSet}.
	 * 
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} is already included in this
	 *             object's {@link FilterSet}.
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} doesn't match any {@link Node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link Filter} is <tt>null</tt>.
	 */
	public synchronized void addFilter(Filter filter)
			throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Filter.class.getCanonicalName()
					+ ".");
		}
		if (getFilters().contains(filter)) {
			throw new IllegalFilterException(Messages.bind(
					Messages.FilteredDocEx_DUPLICATE, filter));
		}
		getFilters().add(filter);
		applyFilter(filter);
	}

	/**
	 * @param filters
	 *            is a {@link FilterSet} to set in this object's
	 *            {@link FilterSet}.
	 * 
	 * @throws IllegalFilterException
	 *             if one {@link Filter} is already included in this object's
	 *             {@link FilterSet}.
	 * @throws IllegalFilterException
	 *             if one {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if one {@link Filter} doesn't match any {@link Node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link FilterSet} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if one {@link FilterSet} is <tt>null</tt>.
	 */
	public synchronized void setFilterSet(FilterSet filters)
			throws IllegalFilterException {
		if (filters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FilterSet.class.getCanonicalName()
					+ ".");
		}
		clearFilters();
		addFilters(filters);
	}

	/**
	 * @param i
	 *            is the position in this object's {@link FilterSet} to place
	 *            the given {@link Filter}.
	 * @param filter
	 *            is an XPath Expression to place in this object's
	 *            {@link FilterSet}.
	 * 
	 * @return the previous {@link Filter} located at the given position.
	 * 
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} doesn't match any {@link Node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link Filter} is <tt>null</tt>.
	 * @throws IndexOutOfBoundsException
	 *             if the given position is out of this object's
	 *             {@link FilterSet}'s range.
	 */
	public synchronized Filter setFilter(int i, Filter filter)
			throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Filter.class.getCanonicalName()
					+ ".");
		}
		restoreOriginalDocument();
		Filter sRemovedFilter = getFilters().set(i, filter);
		applyFilters();
		return sRemovedFilter;
	}

	/**
	 * <p>
	 * Remove all {@link Filter} of this object's {@link FilterSet}.
	 * </p>
	 */
	public synchronized void clearFilters() {
		restoreOriginalDocument();
		getFilters().clear();
	}

	/**
	 * <p>
	 * Remove the {@link Filter} which is located at the given position.
	 * </p>
	 * 
	 * @param i
	 *            is the position of the {@link Filter} to remove.
	 * 
	 * @return the removed {@link Filter}.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the given position is out of this object's
	 *             {@link FilterSet}'s range.
	 */
	public synchronized Filter removeFilter(int i) {
		restoreOriginalDocument();
		Filter sRemovedFilter = getFilters().remove(i);
		try {
			applyFilters();
		} catch (IllegalFilterException Ex) {
			throw new RuntimeException("Unexecpted error while removing "
					+ "a Filter. "
					+ "Because a Filter have been removed, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		return sRemovedFilter;
	}

	/**
	 * @return the number of{@link Filter} hold by this object's
	 *         {@link FilterSet}.
	 */
	public synchronized int countFilters() {
		return _filters.size();
	}

	/**
	 * <p>
	 * Reduce this object to the subset {@link Node}s whose match the
	 * {@link FiltersSet}.
	 * </p>
	 * 
	 * @throws IllegalFilterException
	 *             if one {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if one {@link Filter} doesn't match any {@link Node}s.
	 */
	public synchronized void applyFilters() throws IllegalFilterException {
		for (Filter filter : getFilters()) {
			applyFilter(filter);
		}
	}

	/**
	 * <p>
	 * Reduce this object to the subset {@link Node}s whose match the given
	 * {@link Filter}.
	 * </p>
	 * 
	 * @param filter
	 *            is an XPath Expression, which match some {@link Node}s.
	 * 
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if the given {@link Filter} doesn't match any {@link Node}s.
	 */
	private synchronized void applyFilter(Filter filter)
			throws IllegalFilterException {
		if (getDocument() == null) {
			return;
		}
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Filter.class.getCanonicalName()
					+ ".");
		}

		NodeList nl;
		try {
			nl = evaluateAsNodeList(filter.getValue());
		} catch (XPathExpressionException Ex) {
			throw new IllegalFilterException(Messages.bind(
					Messages.FilteredDocEx_INCORRECT_XPATH, filter.getValue()),
					Ex);
		}
		if (nl.getLength() == 0) {
			throw new IllegalFilterException(Messages.bind(
					Messages.FilteredDocEx_TOO_RSTRICTIVE, filter.getValue()));
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new IllegalFilterException(Messages.bind(
						Messages.FilteredDocEx_MUST_TARGET_ELEMENT,
						filter.getValue(), DocHelper.parseNodeType(nl.item(i))));
			}
		}

		Document oFilteredDoc = DocHelper.newDocument();
		for (int i = 0; i < nl.getLength(); i++) {
			FilteredDocHelper.importNodeIntoFilteredDocument(oFilteredDoc,
					(Element) nl.item(i), true);
		}

		setDocument(oFilteredDoc);
	}

	/**
	 * A element node have been inserted in the current document => modify the
	 * original document.
	 */
	@Override
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
		// insert the node in the original doc
		Document d = getOriginalDocument();
		Element pori = DUNIDDocHelper.getElement(d, pdunid);
		pori.insertBefore(d.importNode(t, true),
				DUNIDDocHelper.getElement(d, sdunid));
		// TODO : how to applyFilters ?
	}

	/**
	 * An element node have been removed in the current document => modify the
	 * original document
	 */
	@Override
	protected void nodeRemoved(MutationEvent evt) throws MelodyException {
		super.nodeRemoved(evt);
		// the removed node
		Element t = (Element) evt.getTarget();
		DUNID tdunid = DUNIDDocHelper.getDUNID(t);
		// its parent node
		Element p = (Element) t.getParentNode();
		DUNID pdunid = DUNIDDocHelper.getDUNID(p);
		// remove the node in the original doc
		Document d = getOriginalDocument();
		Element pori = DUNIDDocHelper.getElement(d, pdunid);
		pori.removeChild(DUNIDDocHelper.getElement(d, tdunid));
		// TODO : how to applyFilters ?
	}

	/**
	 * The text of an element node have been modified in the current document =>
	 * modify the original document
	 */
	@Override
	protected void nodeTextChanged(MutationEvent evt) throws MelodyException {
		super.nodeTextChanged(evt);
		// the changed node
		Text t = (Text) evt.getTarget();
		// its parent node
		Element e = (Element) t.getParentNode();
		DUNID edunid = DUNIDDocHelper.getDUNID(e);
		// change the node in the original doc
		Element eori = DUNIDDocHelper.getElement(getOriginalDocument(), edunid);
		eori.setTextContent(t.getTextContent());
		// TODO : how to applyFilters ?
	}

	/**
	 * An attribute have been inserted in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeInserted(MutationEvent evt) throws MelodyException {
		super.attributeInserted(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID dunid = DUNIDDocHelper.getDUNID(t);
		// insert the attribute in the original doc
		Element n = DUNIDDocHelper.getElement(getOriginalDocument(), dunid);
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	/**
	 * An attribute have been removed in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeRemoved(MutationEvent evt) throws MelodyException {
		super.attributeRemoved(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID dunid = DUNIDDocHelper.getDUNID(t);
		// remove the attribute in the original doc
		Element n = DUNIDDocHelper.getElement(getOriginalDocument(), dunid);
		n.removeAttribute(evt.getAttrName());
	}

	/**
	 * An attribute have been modified in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeModified(MutationEvent evt) throws MelodyException {
		super.attributeModified(evt);
		// the target element
		Element t = (Element) evt.getTarget();
		DUNID dunid = DUNIDDocHelper.getDUNID(t);
		// modify the attribute in the original doc
		Element n = DUNIDDocHelper.getElement(getOriginalDocument(), dunid);
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

}
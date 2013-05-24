package com.wat.melody.common.xml;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.xml.exception.FilteredDocException;
import com.wat.melody.common.xml.exception.IllegalDocException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDoc extends DUNIDDoc {

	/**
	 * XML attribute of an XML Element, which reference an other XML Element.
	 */
	public static final String HERIT_ATTR = "herit";

	protected static Node importNodeIntoFilteredDocument(Document dest,
			Element toImport, boolean importChilds) {
		if (toImport == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid Node.");
		}
		if (toImport.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(toImport.getNodeName()
					+ ": Not accepted. " + "Must be valid Element Node.");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}

		Node imported = getElement(dest, getDUNID(toImport));
		if (imported != null) {
			return imported;
		}

		Node destParent = dest;
		if (toImport.getParentNode().getParentNode() != null) {
			destParent = importNodeIntoFilteredDocument(dest,
					(Element) toImport.getParentNode(), false);
		}
		imported = cloneNodesAndChilds(dest, toImport, importChilds);
		destParent.appendChild(imported);

		return imported;
	}

	private static Node cloneNodesAndChilds(Document dest, Element toClone,
			boolean cloneChilds) {
		FilteredDocHelper.importHeritedParentNode(dest, toClone);

		Node copy = dest.importNode(toClone, false);
		if (cloneChilds) {
			for (int i = 0; i < toClone.getChildNodes().getLength(); i++) {
				Element child = (Element) toClone.getChildNodes().item(i);
				copy.appendChild(cloneNodesAndChilds(dest, child, true));
			}
		}
		return copy;
	}

	private Document moOriginalDOM;
	private FilterSet maFilters;

	public FilteredDoc() {
		super();
		initOriginalDocument();
		initFilters();
	}

	private Document initOriginalDocument() {
		return moOriginalDOM = null;
	}

	private void initFilters() {
		maFilters = new FilterSet();
	}

	protected Document getOriginalDocument() {
		return moOriginalDOM;
	}

	protected Document setOriginalDocument(Document doc) {
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Doc.");
		}
		Document previous = getOriginalDocument();
		moOriginalDOM = doc;
		return previous;
	}

	protected Document cloneOriginalDocument() {
		return (Document) getOriginalDocument().cloneNode(true);
	}

	/**
	 * <p>
	 * Load this object based on the content of the file points by the given
	 * path.
	 * </p>
	 * 
	 * <p>
	 * <i> * Will also apply filters. </i>
	 * </p>
	 * 
	 * @param sPath
	 * 
	 * @throws IllegalDocException
	 *             {@inheritDoc}
	 * @throws IllegalFileException
	 *             {@inheritDoc}
	 * @throws IllegalFilterException
	 *             if a filter is not valid or doesn't match any nodes.
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
	 * Load this object based on the given {@link DUNIDDoc}.
	 * </p>
	 * 
	 * <ul>
	 * <li>Will also apply filters ;</li>
	 * <li>Further modification of this object doesn't affect the given
	 * {@link DUNIDDoc} ;</li>
	 * </ul>
	 * 
	 * @param doc
	 * 
	 * @throws IllegalFilterException
	 *             if a filter is not valid or doesn't match any nodes.
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
	 *             if a filter is not valid or doesn't match any nodes.
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
	 * <p>
	 * Raise an exception if one or more XML Element have an invalid
	 * {@link #HERIT_ATTR} XML Attribute. {@link #HERIT_ATTR} XML Attribute is a
	 * reserved attribute, which allow to define node heritage.
	 * </p>
	 * 
	 * @throws FilteredDocException
	 *             one or more XML Element have an invalid {@link #HERIT_ATTR}
	 *             XML Attribute (match no nodes, match multiple node, doesn't
	 *             contains a valid xpath expression, circular ref).
	 * @throws IllegalDocException
	 *             {@inheritDoc}
	 */
	@Override
	protected synchronized void validateContent() throws IllegalDocException,
			FilteredDocException {
		super.validateContent();
		validateHeritAttrs();
	}

	protected void validateHeritAttrs() throws FilteredDocException {
		FilteredDocHelper.validateParentHeritedNodes(getDocument());
	}

	/**
	 * <p>
	 * Store this object at the given location.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             is the given location is <tt>null</tt>.
	 */
	public synchronized void store(String sPath) {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String.");
		}
		Document memory = getDocument();
		setDocument(getOriginalDocument());
		super.store(sPath);
		setDocument(memory);
	}

	private synchronized FilterSet getFilters() {
		return maFilters;
	}

	public synchronized int countFilters() {
		return maFilters.size();
	}

	public synchronized String getFilter(int i) {
		return maFilters.get(i).getValue();
	}

	public synchronized String removeFilter(int i) {
		if (getOriginalDocument() != null) {
			setDocument(cloneOriginalDocument());
		}
		String sRemovedFilter = getFilters().remove(i).getValue();
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

	public synchronized void clearFilters() {
		if (getOriginalDocument() != null) {
			setDocument(cloneOriginalDocument());
		}
		getFilters().clear();
	}

	public synchronized String setFilter(int i, Filter filter)
			throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Filter.");
		}
		if (getOriginalDocument() != null) {
			setDocument(cloneOriginalDocument());
		}
		String sRemovedFilter = getFilters().set(i, filter).getValue();
		applyFilters();
		return sRemovedFilter;
	}

	public synchronized void setFilters(FilterSet filters)
			throws IllegalFilterException {
		if (filters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid FiltersSet.");
		}
		if (getOriginalDocument() != null) {
			setDocument(cloneOriginalDocument());
		}
		getFilters().clear();
		getFilters().addAll(filters);
		applyFilters();
	}

	/**
	 * 
	 * @param filter
	 *            is a XPath Expression to add to the FilterSet owned by this
	 *            object.
	 * 
	 * @throws IllegalFilterException
	 *             if the filter is not a valid XPath expression.
	 * @throws IllegalFilterException
	 *             if the filter doesn't match any nodes.
	 */
	public synchronized void addFilter(Filter filter)
			throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Filter.");
		}
		getFilters().add(filter);
		applyFilter(filter);
	}

	public synchronized void addFilters(FilterSet filters)
			throws IllegalFilterException {
		if (filters == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid FiltersSet.");
		}
		getFilters().addAll(filters);
		applyFilters();
	}

	/**
	 * <p>
	 * Reduce this object to the {@link Node}s whose match the given
	 * {@link Filter}.
	 * </p>
	 * 
	 * @param filter
	 *            is an XPath Expression, which match some {@link Node}s.
	 * 
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
					+ "Must be a valid Filter.");
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
						filter.getValue(), Doc.parseNodeType(nl.item(i))));
			}
		}

		Document oFilteredDoc = newDocument();
		for (int i = 0; i < nl.getLength(); i++) {
			importNodeIntoFilteredDocument(oFilteredDoc, (Element) nl.item(i),
					true);
		}

		setDocument(oFilteredDoc);
	}

	/**
	 * <p>
	 * Reduce this object to the {@link Node}s whose match the
	 * {@link FiltersSet}.
	 * </p>
	 * 
	 * @throws IllegalFilterException
	 *             if one {@link Filter} of the {@link FilterSet} doesn't match
	 *             any {@link Node}s.
	 */
	protected synchronized void applyFilters() throws IllegalFilterException {
		for (Filter filter : getFilters()) {
			applyFilter(filter);
		}
	}

	@Override
	protected void nodeInstered(MutationEvent evt) {
		super.nodeInstered(evt);
	}

	@Override
	protected void nodeRemoved(MutationEvent evt) {
		super.nodeRemoved(evt);
	}

	@Override
	protected void nodeTextChanged(MutationEvent evt) {
		super.nodeTextChanged(evt);
	}

	/**
	 * An attribute have been inserted in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeInserted(MutationEvent evt) {
		super.attributeInserted(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		Element n = (Element) getElement(getOriginalDocument(), dunid);
		if (n == null) {
			throw new RuntimeException("Unexecpted error while searching the "
					+ "node '" + dunid + "' in the Original Document. "
					+ "This is error is raised because the Current Document "
					+ "and the Original Document are not consistent. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

	/**
	 * An attribute have been removed in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeRemoved(MutationEvent evt) {
		super.attributeRemoved(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		Element n = (Element) getElement(getOriginalDocument(), dunid);
		if (n == null) {
			throw new RuntimeException("Unexecpted error while searching the "
					+ "node '" + dunid + "' in the Original Document. "
					+ "This is error is raised because the Current Document "
					+ "and the Original Document are not consistent. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		n.removeAttribute(evt.getAttrName());
	}

	/**
	 * An attribute have been modified in the current document => modify the
	 * original document
	 */
	@Override
	protected void attributeModified(MutationEvent evt) {
		super.attributeModified(evt);
		Element t = (Element) evt.getTarget();
		DUNID dunid = getDUNID(t);
		Element n = (Element) getElement(getOriginalDocument(), dunid);
		if (n == null) {
			throw new RuntimeException("Unexecpted error while searching the "
					+ "node '" + dunid + "' in the Original Document. "
					+ "This is error is raised because the Current Document "
					+ "and the Original Document are not consistent. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
		n.setAttribute(evt.getAttrName(), evt.getNewValue());
	}

}
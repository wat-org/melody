package com.wat.melody.common.utils;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.utils.exception.IllegalDocException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.common.utils.exception.IllegalFilterException;
import com.wat.melody.common.utils.exception.MelodyException;
import com.wat.melody.common.utils.exception.NoSuchDUNIDException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDoc extends DUNIDDoc {

	private static Node importNodeIntoFilteredDocument(Document doc,
			Node srcNode, boolean deep) {
		if (srcNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid Node.");
		}
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}

		Node oDestNode = getNode(doc, getDUNID(srcNode));
		if (oDestNode != null) {
			return oDestNode;
		}

		Node oDestNodeParent = doc;
		if (srcNode.getParentNode().getParentNode() != null) {
			oDestNodeParent = importNodeIntoFilteredDocument(doc,
					srcNode.getParentNode(), false);
		}
		oDestNode = doc.importNode(srcNode, deep);
		oDestNodeParent.appendChild(oDestNode);

		return oDestNode;
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
	 * <p>
	 * <i> * Will also apply filters. <BR/>
	 * * Further modification of this object doesn't affect the given
	 * {@link DUNIDDoc} ; <BR/>
	 * </i>
	 * </p>
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

	public synchronized void store() {
		store(getFileFullPath());
	}

	public synchronized void store(String sPath) {
		Document doc = (Document) getOriginalDocument().cloneNode(true);
		NodeList nl = findDUNIDs(doc);
		for (int i = 0; i < nl.getLength(); i++) {
			nl.item(i).getAttributes().removeNamedItem(DUNID_ATTR);
		}
		super.store(doc, sPath);
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
					Messages.FilterEx_INCORRECT_XPATH, filter.getValue()), Ex);
		}
		if (nl.getLength() == 0) {
			throw new IllegalFilterException(Messages.bind(
					Messages.FilterEx_TOO_RSTRICTIVE, filter.getValue()));
		}
		Document oFilteredDoc = newDocument();
		for (int i = 0; i < nl.getLength(); i++) {
			importNodeIntoFilteredDocument(oFilteredDoc, nl.item(i), true);
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
		setAttributeValue(getDocument(), ownerNodeDUNID, sAttrName, sAttrValue);

		try {
			return setAttributeValue(getOriginalDocument(), ownerNodeDUNID,
					sAttrName, sAttrValue);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexecpted error while searching the "
					+ "node '" + ownerNodeDUNID + "' in the Original "
					+ "Document. "
					+ "This is error is raised because the Current Document "
					+ "and the Original Document are not consistent. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
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
		removeAttribute(getDocument(), ownerNodeDUNID, sAttrName);
		try {
			return removeAttribute(getOriginalDocument(), ownerNodeDUNID,
					sAttrName);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexecpted error while searching the "
					+ "node '" + ownerNodeDUNID + "' in the Original "
					+ "Document. "
					+ "This is error is raised because the Current Document "
					+ "and the Original Document are not consistent. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

}

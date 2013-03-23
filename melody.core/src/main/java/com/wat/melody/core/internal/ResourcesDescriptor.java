package com.wat.melody.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetFilterException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.DUNIDDoc;
import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesDescriptor extends FilteredDoc implements
		IResourcesDescriptor {

	private TargetDescriptor moTargetDescriptor;
	private List<DUNIDDoc> moDUNIDDocList;

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
		Node root = getOriginalDocument().createElement("resources");
		getOriginalDocument().appendChild(root);
		// Add it an DUNIND XML attribute
		createAttribute(DUNID_ATTR, new DUNID(0).toString(), root);
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
		return moTargetDescriptor;
	}

	private TargetDescriptor setTargetDescriptor(TargetDescriptor td) {
		if (td == null) {
			throw new IllegalArgumentException("nul: Not accepted. "
					+ "Must be a valid TargetDescriptor.");
		}
		TargetDescriptor previous = getTargetDescriptor();
		moTargetDescriptor = td;
		return previous;
	}

	private List<DUNIDDoc> getDUNIDDocList() {
		return moDUNIDDocList;
	}

	private List<DUNIDDoc> setDUNIDDocList(List<DUNIDDoc> srds) {
		if (srds == null) {
			throw new IllegalArgumentException("nul: Not accepted. "
					+ "Must be a valid List<DUNIDDoc>.");
		}
		List<DUNIDDoc> previous = getDUNIDDocList();
		moDUNIDDocList = srds;
		return previous;
	}

	@Override
	public String toString() {
		String s = null;
		s += "Resources Descriptor(s)" + "=";
		for (DUNIDDoc rd : getDUNIDDocList()) {
			s += rd.getFileFullPath() + ", ";
		}
		s += Util.NEW_LINE;
		s += "Resources Filter(s)" + "=";
		for (int i = 0; i < countFilters(); i++) {
			s += getFilter(i) + ", ";
		}
		s += Util.NEW_LINE;
		s += "Target Filter(s)" + "=";
		for (int i = 0; i < countTargetsFilters(); i++) {
			s += getTargetsFilter(i) + ", ";
		}
		return s;
	}

	@Override
	public synchronized DUNID getMelodyID(Node n) {
		return getDUNID(n);
	}

	@Override
	public List<Node> evaluateTargets(String xpath)
			throws XPathExpressionException {
		List<Node> targets = new ArrayList<Node>();
		// Evaluate expression in the current document
		NodeList nl = evaluateAsNodeList(xpath);
		// Search for resulting nodes in the eligible targets
		for (int i = 0; i < nl.getLength(); i++) {
			if (getTargetDescriptor().getNode(getDUNID(nl.item(i))) != null) {
				targets.add(nl.item(i));
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
			d.load(sPath);
			getDUNIDDocList().add(d);
			// Add the content
			Node n = getOriginalDocument().importNode(
					d.getDocument().getFirstChild(), true);
			getOriginalDocument().getFirstChild().appendChild(n);
			// Rebuild
			setDocument(cloneOriginalDocument());
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
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException {
		if (sPath == null) {
			return false;
		}
		int i = -1;
		// Search the doc in the list
		while (++i < getDUNIDDocList().size()) {
			DUNIDDoc d = getDUNIDDocList().get(i);
			if (d.getFileFullPath().equals(sPath)) {
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
		// Rebuild
		setDocument(cloneOriginalDocument());
		try {
			applyFilters();
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
		// Update targetDesc
		getTargetDescriptor().load(this);
		return true;
	}

	@Override
	public synchronized void store() {
		for (DUNIDDoc d : getDUNIDDocList()) {
			d.store();
		}
	}

	@Override
	public synchronized String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsString(sXPathExpr, getDocument().getFirstChild());
	}

	@Override
	public synchronized NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNodeList(sXPathExpr, getDocument().getFirstChild());
	}

	@Override
	public synchronized Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNode(sXPathExpr, getDocument().getFirstChild());
	}

	@Override
	public synchronized String setAttributeValue(DUNID sOwnerNodeDUNID,
			String sAttrName, String sAttrValue) throws NoSuchDUNIDException {
		try {
			getTargetDescriptor().setAttributeValue(sOwnerNodeDUNID, sAttrName,
					sAttrValue);
		} catch (NoSuchDUNIDException Ex) {
			// This DUDID can not be in the target descriptor : it is normal
			// There is no error
		}
		DUNIDDoc d = getOwnerDUNIDDoc(sOwnerNodeDUNID);
		d.setAttributeValue(sOwnerNodeDUNID, sAttrName, sAttrValue);
		return super.setAttributeValue(sOwnerNodeDUNID, sAttrName, sAttrValue);
	}

	@Override
	public synchronized String removeAttribute(DUNID sOwnerNodeDUNID,
			String sAttrName) throws NoSuchDUNIDException {
		try {
			getTargetDescriptor().removeAttribute(sOwnerNodeDUNID, sAttrName);
		} catch (NoSuchDUNIDException Ex) {
			// This DUNID can not be in the target descriptor : it is normal
			// There is no error
		}
		DUNIDDoc d = getOwnerDUNIDDoc(sOwnerNodeDUNID);
		d.removeAttribute(sOwnerNodeDUNID, sAttrName);
		return super.removeAttribute(sOwnerNodeDUNID, sAttrName);
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
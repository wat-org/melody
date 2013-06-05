package com.wat.melody.api;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetsFilterException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.exception.IllegalDocException;

/**
 * <p>
 * Managed a set of resources, described in xml files.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IResourcesDescriptor {

	/**
	 * <p>
	 * Add the given xml file to the resources managed by this object.
	 * </p>
	 * 
	 * @return <tt>true</tt> it the given file was successfully added from the
	 *         resources managed by this object, or <tt>false</tt> in any other
	 *         case (the given file is <tt>null</tt>, the given file is already
	 *         managed by this object).
	 * 
	 * @throws IllegalFileException
	 *             if a the given xml file is not a valid file.
	 * @throws IllegalDocException
	 *             if the resources are not valid (ex : dunid_attr already
	 *             present, herit_attr linkage err).
	 * @throws IllegalResourcesFilterException
	 *             if a filter is not valid (no nodes match, invalid XPath
	 *             Expression).
	 * @throws IllegalTargetsFilterException
	 *             if a target filter is not valid (no nodes match, invalid
	 *             XPath Expression).
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public boolean add(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalTargetsFilterException,
			IllegalResourcesFilterException, IOException;

	/**
	 * @param sPath
	 *            is the path of a previously added (see {@link #add(String)})
	 *            xml file.
	 * 
	 * @return <tt>true</tt> it the given file was found and successfully
	 *         removed from the resources managed by this object, or
	 *         <tt>false</tt> in any other case.
	 * 
	 * @throws IllegalDocException
	 *             if, once the given file remove, the resulting resources are
	 *             not valid (ex : herit_attr linkage err).
	 * @throws IllegalResourcesFilterException
	 *             if, once the given file remove, a filter is no more valid
	 *             (e.g. no nodes match).
	 * @throws IllegalTargetsFilterException
	 *             if, once the given file remove, a target filter is no more
	 *             valid (e.g. no nodes match).
	 */
	public boolean remove(String sPath) throws IllegalDocException,
			IllegalTargetsFilterException, IllegalResourcesFilterException;

	public String dump();

	public String fulldump();

	public void store();

	/**
	 * @param n
	 *            is an {@link Element}.
	 * 
	 * @return the {@link DUNID} of the given {@link Element}, or <tt>null</tt>
	 *         if the given {@link Element} is <tt>null</tt>.
	 */
	public DUNID getMelodyID(Element n);

	/**
	 * @param melodyId
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Element} whose match the given {@link DUNID} if found,
	 *         or <tt>null</tt> if such {@link Element} cannot be found or if
	 *         the given {@link DUNID} is <tt>null</tt>..
	 */
	public Element getElement(DUNID melodyId);

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException;

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException;

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException;

	/**
	 * @param xpath
	 *            is an XPath Expression.
	 * 
	 * @return all {@link Element}s whose match the given XPath Expression from
	 *         all eligible targets.
	 * 
	 * @throws XPathExpressionException
	 *             if the given XPath Expression is not valid.
	 */
	public List<Element> evaluateTargets(String xpath)
			throws XPathExpressionException;

	/**
	 * @return a shallow copy of this object's {@link FilterSet} (The elements
	 *         themselves are not copied; If the returned {@link FilterSet} is
	 *         modified, this object's {@link FilterSet} will not be modified).
	 */
	public FilterSet getFilterSet();

	public Filter getFilter(int i);

	public void addFilters(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException;

	public void addFilter(Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException;

	public void setFilterSet(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException;

	public Filter setFilter(int i, Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetsFilterException;

	public void clearFilters();

	public Filter removeFilter(int i);

	public int countFilters();

	/**
	 * @return a shallow copy of this object's Target {@link FilterSet} (The
	 *         elements themselves are not copied; If the returned
	 *         {@link FilterSet} is modified, this object's Target
	 *         {@link FilterSet} will not be modified).
	 */
	public FilterSet getTargetFilterSet();

	public Filter getTargetFilter(int i);

	public void addTargetFilters(FilterSet filters)
			throws IllegalTargetsFilterException;

	public void addTargetFilter(Filter filter)
			throws IllegalTargetsFilterException;

	public void setTargetFilterSet(FilterSet filters)
			throws IllegalTargetsFilterException;

	public Filter setTargetFilter(int i, Filter filter)
			throws IllegalFilterException;

	public void clearTargetFilters();

	public Filter removeTargetFilter(int i);

	public int countTargetFilters();

}
package com.wat.melody.api;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetFilterException;
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
	 * @throws IllegalFileException
	 *             if a the given xml file is not a valid file.
	 * @throws IllegalDocException
	 *             if the resources are not valid (ex : dunid_attr already
	 *             present, herit_attr linkage err).
	 * @throws IllegalResourcesFilterException
	 *             if a filter is not valid (no nodes match, invalid XPath
	 *             Expression).
	 * @throws IllegalTargetFilterException
	 *             if a target filter is not valid (no nodes match, invalid
	 *             XPath Expression).
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public void add(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalTargetFilterException,
			IllegalResourcesFilterException, IOException;

	/**
	 * <p>
	 * Remove the given xml file to the resources managed by this object.
	 * </p>
	 * 
	 * @return <tt>true</tt> it the given file was found and successfully
	 *         removed, or <tt>false</tt> in any other case.
	 * 
	 * @throws IllegalDocException
	 *             if, once the given file remove, the resulting resources are
	 *             not valid (ex : herit_attr linkage err).
	 * @throws IllegalResourcesFilterException
	 *             if, once the given file remove, a filter is no more valid
	 *             (e.g. no nodes match).
	 * @throws IllegalTargetFilterException
	 *             if, once the given file remove, a target filter is no more
	 *             valid (e.g. no nodes match).
	 */
	public boolean remove(String sPath) throws IllegalDocException,
			IllegalTargetFilterException, IllegalResourcesFilterException;

	public void store();

	/**
	 * <p>
	 * Get the {@link DUNID} of the given {@link Element}.
	 * </p>
	 * 
	 * @param n
	 *            is a {@link Element} owned by this object.
	 * 
	 * @return the {@link DUNID} of the given {@link Element}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is not owned by this object.
	 * @throws RuntimeException
	 *             if the given {@link Element} doesn't have a {@link DUNID}
	 *             attribute, or if the attribute's value is not valid
	 *             {@link DUNID}.
	 */
	public DUNID getMelodyID(Element n);

	/**
	 * <p>
	 * Get the {@link Element} whose match the given {@link DUNID}.
	 * </p>
	 * 
	 * @param melodyID
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Element} whose match the given {@link DUNID} if found,
	 *         <code>null</code> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 */
	public Element getElement(DUNID melodyID);

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException;

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException;

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException;

	/**
	 * <p>
	 * Returns all {@link Element}s whose match the given XPath Expression from
	 * all eligible targets.
	 * </p>
	 * 
	 * @param xpath
	 *            is the XPath Expression to evaluate.
	 * 
	 * @return all {@link Element}s whose match the given XPath Expression from
	 *         all eligible targets.
	 * 
	 * @throws XPathExpressionException
	 *             if the given XPath Expression is not valid.
	 */
	public List<Element> evaluateTargets(String xpath)
			throws XPathExpressionException;

	public String getFilter(int i);

	public void addFilter(Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException;

	public void addFilters(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException;

	public String setFilter(int i, Filter filter)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException;

	public void setFilters(FilterSet filters)
			throws IllegalResourcesFilterException,
			IllegalTargetFilterException;

	public String removeFilter(int i);

	public void clearFilters();

	public int countFilters();

	public String getTargetsFilter(int i);

	public void addTargetsFilter(Filter filter)
			throws IllegalTargetFilterException;

	public void addTargetsFilters(FilterSet filters)
			throws IllegalTargetFilterException;

	public String setTargetsFilter(int i, Filter filter)
			throws IllegalFilterException;

	public void setTargetsFilters(FilterSet filters)
			throws IllegalTargetFilterException;

	public String removeTargetsFilter(int i);

	public void clearTargetsFilters();

	public int countTargetsFilters();

}
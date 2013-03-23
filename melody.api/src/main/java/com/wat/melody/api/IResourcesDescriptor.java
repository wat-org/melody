package com.wat.melody.api;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

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
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;

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
	 */
	public boolean remove(String sPath) throws IllegalTargetFilterException,
			IllegalResourcesFilterException;

	public void store();

	/**
	 * <p>
	 * Get the {@link DUNID} of the given {@link Node}.
	 * </p>
	 * 
	 * @param n
	 *            is a {@link Node} owned by this object.
	 * 
	 * @return the {@link DUNID} of the given {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is not owned by this object.
	 * @throws RuntimeException
	 *             if the given {@link Node} doesn't have a {@link DUNID}
	 *             attribute, or if the attribute's value is not valid
	 *             {@link DUNID}.
	 */
	public DUNID getMelodyID(Node n);

	/**
	 * <p>
	 * Get the Node whose match the given {@link DUNID}.
	 * </p>
	 * 
	 * @param melodyID
	 *            is the {@link DUNID} to search.
	 * 
	 * @return the {@link Node} whose match the given {@link DUNID} if found,
	 *         <code>null</code> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 */
	public Node getNode(DUNID melodyID);

	/*
	 * TODO : expand the resulting value.
	 */
	/**
	 * <p>
	 * Get the requested attribute's value of the requested {@link Node}.
	 * </p>
	 * 
	 * @param sOwnerNodeDUNID
	 *            is the {@link DUNID} of the requested {@link Node}.
	 * @param sAttrName
	 *            is the name of the requested attribute.
	 * 
	 * @return a {@link String}, which contains the value of the requested
	 *         {@link Node}'s attribute, or <code>null</code> if this object
	 *         have not been loaded yet or if the requested {@link Node}'s
	 *         attribute doesn't exists.
	 * 
	 * @throws NoSuchDUNIDException
	 *             if the given {@link DUNID} cannot be found in any
	 *             {@link node}s.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public String getAttributeValue(DUNID sOwnerNodeDUNID, String sAttrName)
			throws NoSuchDUNIDException;

	/**
	 * <p>
	 * Set the attribute's value of the requested {@link Node}. Create the
	 * attribute if it doesn't exist.
	 * </p>
	 * 
	 * @param sOwnerNodeDUNID
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
	 *             if the given {@link DUNID} cannot be found in any
	 *             {@link Node}.
	 * @throws IllegalArgumentException
	 *             if the given {@link DUNID} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given {@link String} is <code>null</code>.
	 */
	public String setAttributeValue(DUNID sOwnerNodeDUNID, String sAttrName,
			String sAttrValue) throws NoSuchDUNIDException;

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
	public String removeAttribute(DUNID sOwnerNodeDUNID, String sAttrName)
			throws NoSuchDUNIDException;

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException;

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException;

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException;

	/**
	 * <p>
	 * Returns all {@link Node}s whose match the given XPath Expression from all
	 * eligible targets.
	 * </p>
	 * 
	 * @param xpath
	 *            is the XPath Expression to evaluate.
	 * 
	 * @return all {@link Node}s whose match the given XPath Expression from all
	 *         eligible targets.
	 * 
	 * @throws XPathExpressionException
	 *             if the given XPath Expression is not valid.
	 */
	public List<Node> evaluateTargets(String xpath)
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
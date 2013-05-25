package com.wat.melody.api;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.xml.exception.IllegalDocException;

/**
 * <p>
 * A Sequence Descriptor contains the processing instructions the
 * {@link IProcessorManager} will process.
 * </p>
 * <p>
 * Only registered {@link OrderName} will be proceed.
 * </p>
 * <p>
 * All relative path defined in the processing instructions are resolved against
 * this object baseDir. If not specified,the default basedir is the directory
 * where this file is located.
 * </p>
 * <p>
 * Registered {@link Property} will be used for the processing instructions
 * expansion.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISequenceDescriptor {

	/**
	 * <p>
	 * Load the Sequence Descriptor defined in the given file.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't points to a valid file.
	 * @throws IllegalOrderException
	 *             if, at least, one {@link OrderName} previously registered is
	 *             not valid.
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 * @throws IOException
	 *             if an IO error occurred while reading the file pointed by the
	 *             given path.
	 */
	public void load(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException, IOException;

	/**
	 * <p>
	 * Load the Sequence Descriptor from the content of the given
	 * {@link ISequenceDescriptor}.
	 * </p>
	 * 
	 * @param sd
	 *            is the {@link ISequenceDescriptor} to copy.
	 * 
	 * @throws IllegalOrderException
	 *             if, at least, one {@link OrderName} previously registered is
	 *             not valid.
	 */
	public void load(ISequenceDescriptor sd) throws IllegalOrderException;

	public String getSourceFile();

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException;

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException;

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException;

	public Element getRoot();

	public File getBaseDir();

	public File setBaseDir(File v) throws IllegalDirectoryException;

	public PropertiesSet getProperties();

	public void setProperties(PropertiesSet ps);

	public void addProperties(PropertiesSet ps);

	public Property addProperty(Property p);

	public void addOrder(OrderName v) throws IllegalOrderException;

	public void addOrders(OrderNameSet orders) throws IllegalOrderException;

	public OrderName setOrder(int i, OrderName order)
			throws IllegalOrderException;

	public void setOrders(OrderNameSet orders) throws IllegalOrderException;

	public int countOrders();

	public OrderName getOrder(int i);

	public void clearOrders();

}
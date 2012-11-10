package com.wat.melody.api;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.utils.OrderName;
import com.wat.melody.common.utils.OrderNameSet;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalDocException;
import com.wat.melody.common.utils.exception.IllegalFileException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISequenceDescriptor {

	public void load(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException, IOException;

	public void load(ISequenceDescriptor sd) throws IllegalOrderException;

	public Document getDocument();

	public String getFileFullPath();

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException;

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException;

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException;

	public Node getRoot();

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
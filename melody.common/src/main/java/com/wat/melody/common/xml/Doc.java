package com.wat.melody.common.xml;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.xml.exception.IllegalDocException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Doc {

	private static DocumentBuilder moBuilder;
	private static XPath moXPath;

	/*
	 * TODO : XQuery doesn't support 'order by' and 'where'....
	 */

	static {
		// Specify we want the 'saxon XPath 2.0 resolver'
		System.setProperty("javax.xml.transform.TransformerFactory",
				"net.sf.saxon.TransformerFactoryImpl");
		moXPath = XPathFactory.newInstance().newXPath();
		try {
			moBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a new Document Builder. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	protected static DocumentBuilder getDocumentBuilder() {
		return moBuilder;
	}

	protected static Document parse(File sPath) throws SAXException,
			IOException {
		return Parser.parse(sPath);
	}

	protected static Document parse(String content) throws SAXException,
			IOException {
		return Parser.parse(content);
	}

	public static XPath getXPath() {
		return moXPath;
	}

	/**
	 * <p>
	 * Create an empty Document.
	 * </p>
	 * 
	 * @return an empty Document.
	 */
	public synchronized static Document newDocument() {
		return getDocumentBuilder().newDocument();
	}

	// evaluate XPath expression as a String, inside a Node (XPath 2.0
	// supported)
	public synchronized static String evaluateAsString(String sXPathExpr,
			Node oNode) throws XPathExpressionException {
		return (String) getXPath().evaluate(sXPathExpr, oNode,
				XPathConstants.STRING);
	}

	// evaluate XPath expression as a NodeList, inside a Node (XPath 2.0
	// supported)
	public synchronized static NodeList evaluateAsNodeList(String sXPathExpr,
			Node oNode) throws XPathExpressionException {
		return (NodeList) getXPath().evaluate(sXPathExpr, oNode,
				XPathConstants.NODESET);
	}

	// evaluate XPath expression as a Node, inside a Node (XPath 2.0 supported)
	public synchronized static Node evaluateAsNode(String sXPathExpr, Node oNode)
			throws XPathExpressionException {
		return (Node) getXPath().evaluate(sXPathExpr, oNode,
				XPathConstants.NODE);
	}

	public static String parseInt(int nodeType) {
		switch (nodeType) {
		case Node.ATTRIBUTE_NODE:
			return "ATTRIBUTE";
		case Node.CDATA_SECTION_NODE:
			return "CDATA_SECTION";
		case Node.COMMENT_NODE:
			return "COMMENT";
		case Node.DOCUMENT_FRAGMENT_NODE:
			return "DOCUMENT_FRAGMENT";
		case Node.DOCUMENT_NODE:
			return "DOCUMENT";
		case Node.DOCUMENT_TYPE_NODE:
			return "DOCUMENT_TYPE";
		case Node.ELEMENT_NODE:
			return "ELEMENT";
		case Node.ENTITY_NODE:
			return "ENTITY";
		case Node.ENTITY_REFERENCE_NODE:
			return "ENTITY_REFERENCE";
		case Node.NOTATION_NODE:
			return "NOTATION";
		case Node.PROCESSING_INSTRUCTION_NODE:
			return "PROCESSING_INSTRUCTION";
		case Node.TEXT_NODE:
			return "TEXT";
		default:
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must a valid int (a Node Type).");
		}
	}

	/**
	 * <p>
	 * Create a new Attribute with the given name, set its value to the given
	 * value and add it to the given Node.
	 * </p>
	 * 
	 * @param sAttrName
	 *            is the name of the attribute to create.
	 * @param sAttrValue
	 *            is the corresponding value of the attribute.
	 * @param oNode
	 *            is the Node where the new Attribute will be added.
	 * 
	 * @return the newly created Attribute.
	 */
	public synchronized static Attr createAttribute(String sAttrName,
			String sAttrValue, Node oNode) {
		if (sAttrName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute name).");
		}
		if (sAttrName.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid String (an XML Attribute name).");
		}
		if (oNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		Attr oAtr = oNode.getOwnerDocument().createAttribute(sAttrName);
		oAtr.setNodeValue(sAttrValue);
		oNode.getAttributes().setNamedItem(oAtr);
		return oAtr;
	}

	/**
	 * <p>
	 * Store the given Document into the given file.
	 * </p>
	 * 
	 * @param oDoc
	 *            is the {@link Document} to store on disk.
	 * @param sPath
	 *            is the path where the {@link Document} will be stored.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <code>null</code>, or if the
	 *             given path is <code>null</code>.
	 */
	public synchronized static void store(Document oDoc, String sPath) {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a file path).");
		}
		if (sPath.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid String (a file path).");
		}
		if (oDoc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(new DOMSource(oDoc), new StreamResult(
					new File(sPath)));
		} catch (TransformerException Ex) {
			throw new RuntimeException("Error while saving XML document "
					+ "to '" + sPath + "'.", Ex);
		}
	}

	/**
	 * <p>
	 * Dump the given Document into a <code>String</code>.
	 * </p>
	 * 
	 * @param oDoc
	 *            is the {@link Document} to dump.
	 * 
	 * @return a <code>String</code>, which is the String representation of the
	 *         given {@link Document}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <code>null</code>.
	 */
	public static String dump(Document oDoc) {
		if (oDoc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(oDoc), new StreamResult(sw));
			return sw.toString();
		} catch (TransformerException Ex) {
			throw new RuntimeException("Error while dumping XML document.", Ex);
		}
	}

	/**
	 * <p>
	 * Get the XPath position of the given <code>Node</code>.
	 * </p>
	 * 
	 * @param n
	 *            is the node
	 * @return an XPath expression which can be used to query the given
	 *         <code>Node</code>.
	 * @throws IllegalArgumentException
	 *             if the given <code>Node</code> is <code>null</code> or is not
	 *             an element <code>Node</code>.
	 */
	public synchronized static String getXPathPosition(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Must be a Element Node.");
		}

		String sTargetXPath = "";
		for (; n.getParentNode() != null; n = n.getParentNode())
			sTargetXPath = "/" + n.getNodeName() + "["
					+ getChildNodePosition(n) + "]" + sTargetXPath;
		return sTargetXPath;
	}

	private synchronized static int getChildNodePosition(Node child) {
		if (child == null) {
			throw new NullPointerException("null: Not accepted. "
					+ "Must be a Node.");
		}
		if (child.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(child.getNodeName()
					+ ": Not accepted. " + "Must be a Element Node.");
		}

		Node parent = child.getParentNode();
		int index = 1;
		for (int i = 0; i < parent.getChildNodes().getLength(); ++i)
			if (parent.getChildNodes().item(i) == child) {
				return index;
			} else if (parent.getChildNodes().item(i).getNodeName()
					.compareTo(child.getNodeName()) == 0) {
				++index;
			}

		throw new RuntimeException("Unexecpted error while looking "
				+ "for a child node position. "
				+ "Source code has certainly been modified and "
				+ "a bug have been introduced.");
	}

	public static Location getNodeLocation(Node n) {
		return new Location(n);
	}

	private String msFFP;
	private Document moDOM;

	public Doc() {
		initFileFullPath();
		initDocument();
	}

	public String dump() {
		return dump(getDocument());
	}

	private String initFileFullPath() {
		return msFFP = null;
	}

	private Document initDocument() {
		return moDOM = null;
	}

	/**
	 * <p>
	 * Load the content of the file points by the given path into this object.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to load.
	 * @throws IllegalFileException
	 *             if the given path doesn't point to a valid file (e.g. the
	 *             file doesn't exists, the path is a directory, ...).
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 * @throws IOException
	 *             {@inheritDoc}
	 */
	public void load(String sPath) throws MelodyException, IllegalDocException,
			IllegalFileException, IOException {
		setFileFullPath(sPath);
		try {
			setDocument(parse(new File(sPath)));
		} catch (SAXParseException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX_AT, new Object[] { sPath,
							Ex.getLineNumber(), Ex.getColumnNumber() }), Ex);
		} catch (SAXException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX, sPath), Ex);
		} catch (CharConversionException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_DATA, sPath), Ex);
		}
		validateContent();
	}

	/**
	 * <p>
	 * Load the given XML content into this object.
	 * </p>
	 * 
	 * @param content
	 *            is an XML String.
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 * @throws IOException
	 *             {@inheritDoc}
	 */
	public void loadFromXML(String content) throws MelodyException,
			IllegalDocException, IOException {
		try {
			setDocument(parse(content));
		} catch (SAXParseException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX_AT,
					new Object[] { "inline content", Ex.getLineNumber(),
							Ex.getColumnNumber() }), Ex);
		} catch (SAXException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX, "inline content"), Ex);
		} catch (CharConversionException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_DATA, "inline content"), Ex);
		}
		validateContent();
	}

	public void load(Doc doc) {
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Doc.");
		}
		try {
			if (doc.getFileFullPath() != null) {
				setFileFullPath(doc.getFileFullPath());
			}
		} catch (IllegalFileException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "setting the Doc File Path to " + "'"
					+ doc.getFileFullPath() + "'. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. "
					+ "Or an external event made the file no more "
					+ "accessible (deleted, moved, read permission "
					+ "removed, ...).", Ex);
		}
		setDocument((Document) doc.getDocument().cloneNode(true));
	}

	/**
	 * <p>
	 * Validate the content of this object.
	 * </p>
	 * 
	 * @throws IllegalDocException
	 *             if the validation fails.
	 */
	protected void validateContent() throws IllegalDocException {
	}

	public String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsString(sXPathExpr, getDocument());
	}

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNodeList(sXPathExpr, getDocument());
	}

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNode(sXPathExpr, getDocument());
	}

	public void store() {
		if (getFileFullPath() == null) {
			return;
		}
		store(getFileFullPath());
	}

	public void store(String sPath) {
		if (getDocument() == null) {
			return;
		}
		store(getDocument(), sPath);
	}

	public String getFileFullPath() {
		return msFFP;
	}

	protected String setFileFullPath(String sPath) throws IllegalFileException {
		FS.validateFileExists(sPath);
		String previous = msFFP;
		msFFP = sPath;
		return previous;
	}

	public Document getDocument() {
		return moDOM;
	}

	protected Document setDocument(Document v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		Document previous = moDOM;
		moDOM = v;
		return previous;
	}

}

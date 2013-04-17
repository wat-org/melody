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
import javax.xml.xpath.XPathExpressionException;

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
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Doc {

	private static DocumentBuilder moBuilder;

	static {
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

	/**
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws SAXException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	protected static Document parse(File sPath) throws SAXException,
			IOException {
		return Parser.parse(sPath);
	}

	/**
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws SAXException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	protected static Document parse(String content) throws SAXException,
			IOException {
		return Parser.parse(content);
	}

	/**
	 * <p>
	 * Create an empty Document.
	 * </p>
	 * 
	 * @return an empty Document.
	 */
	public static Document newDocument() {
		DocumentBuilder builder = getDocumentBuilder();
		synchronized (builder) {
			return builder.newDocument();
		}
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
	public static Attr createAttribute(String sAttrName, String sAttrValue,
			Node oNode) {
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
	 * @param d
	 *            is the {@link Document} to store on disk.
	 * @param path
	 *            is the path where the {@link Document} will be stored.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <code>null</code>, or if the
	 *             given path is <code>null</code>.
	 */
	public static void store(Document d, String path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a file path).");
		}
		if (path.trim().length() == 0) {
			throw new IllegalArgumentException(": Not accepted. "
					+ "Must be a valid String (a file path).");
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		try {
			synchronized (d) {
				TransformerFactory f = TransformerFactory.newInstance();
				Transformer t = f.newTransformer();
				t.transform(new DOMSource(d), new StreamResult(new File(path)));
			}
		} catch (TransformerException Ex) {
			throw new RuntimeException("Error while saving XML document "
					+ "to '" + path + "'.", Ex);
		}
	}

	/**
	 * <p>
	 * Dump the given Document into a <code>String</code>.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to dump.
	 * 
	 * @return a <code>String</code>, which is the String representation of the
	 *         given {@link Document}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <code>null</code>.
	 */
	public static String dump(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Document.");
		}
		try {
			synchronized (d) {
				StringWriter sw = new StringWriter();
				TransformerFactory f = TransformerFactory.newInstance();
				Transformer t = f.newTransformer();
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				t.setOutputProperty(OutputKeys.METHOD, "xml");
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				t.transform(new DOMSource(d), new StreamResult(sw));
				return sw.toString();
			}
		} catch (TransformerException Ex) {
			throw new RuntimeException("Error while dumping XML document.", Ex);
		}
	}

	/**
	 * <p>
	 * Get the XPath position of the given {@link Node}.
	 * </p>
	 * 
	 * @param n
	 *            is the node.
	 * 
	 * @return an XPath Expression, which can be used to query the given
	 *         {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is <code>null</code> or is not an
	 *             Element {@link Node}.
	 */
	public static String getXPathPosition(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Must be a Element Node.");
		}
		String sTargetXPath = "";
		synchronized (n.getOwnerDocument()) {
			for (; n.getParentNode() != null; n = n.getParentNode())
				sTargetXPath = "/" + n.getNodeName() + "["
						+ getChildNodePosition(n) + "]" + sTargetXPath;
		}
		return sTargetXPath;
	}

	private static int getChildNodePosition(Node child) {
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
	private XPath moXPath;

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
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't point to a valid file (e.g. the
	 *             file doesn't exists, the path is a directory, ...).
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
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
	 * Duplicates the given {@link Doc} into this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>Further modification of this object doesn't affect the given
	 * {@link Doc} ;</li>
	 * </ul>
	 * 
	 * @param doc
	 *            is the {@link Doc} to duplicate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Doc} is <tt>null</tt>.
	 */
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
	 * Load the given XML content into this object.
	 * </p>
	 * 
	 * @param content
	 *            is an XML String.
	 * 
	 * @throws IllegalDocException
	 *             if the given XML content is not valid.
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
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
		return XPathExpander.evaluateAsString(sXPathExpr, getDocument(),
				getXPath());
	}

	public NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNodeList(sXPathExpr, getDocument(),
				getXPath());
	}

	public Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNode(sXPathExpr, getDocument(),
				getXPath());
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
		String previous = getFileFullPath();
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
		Document previous = getDocument();
		moDOM = v;
		return previous;
	}

	public XPath getXPath() {
		return moXPath;
	}

	/**
	 * if null, xpath expression will be evaluate without any custom xpath
	 * function resolution.
	 */
	public XPath setXPath(XPath v) {
		XPath previous = getXPath();
		moXPath = v;
		return previous;
	}

}

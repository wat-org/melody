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
	protected static Document parse(File path) throws SAXException, IOException {
		return Parser.parse(path);
	}

	/**
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws SAXException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	protected static Document parse(String xml) throws SAXException,
			IOException {
		return Parser.parse(xml);
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

	public static String parseNodeType(Node n) {
		switch (n.getNodeType()) {
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
					+ "Must a valid " + Node.class.getCanonicalName() + ".");
		}
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
	 *             if the given {@link Document} is <tt>null</tt>, or if the
	 *             given path is <tt>null</tt>.
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
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
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
	 *             if the given {@link Document} is <tt>null</tt>.
	 */
	public static String dump(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
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
	 *             if the given {@link Node} is <tt>null</tt> or is not an
	 *             Element {@link Node}.
	 */
	public static String getXPathPosition(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(parseNodeType(n)
					+ ": Not accepted. " + "Must be an Element Node.");
		}
		String sTargetXPath = "";
		synchronized (n.getOwnerDocument()) {
			for (; n.getParentNode() != null; n = n.getParentNode())
				sTargetXPath = "/" + n.getNodeName() + "["
						+ getChildNodePosition(n) + "]" + sTargetXPath;
		}
		return sTargetXPath;
	}

	private static int getChildNodePosition(Node n) {
		if (n == null) {
			throw new NullPointerException("null: Not accepted. "
					+ "Must be a " + Node.class.getCanonicalName() + ".");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(parseNodeType(n)
					+ ": Not accepted. " + "Must be an Element Node.");
		}

		Node parent = n.getParentNode();
		int index = 1;
		for (int i = 0; i < parent.getChildNodes().getLength(); ++i)
			if (parent.getChildNodes().item(i) == n) {
				return index;
			} else if (parent.getChildNodes().item(i).getNodeName()
					.compareTo(n.getNodeName()) == 0) {
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

	private static String XPATH = "x";

	/**
	 * <p>
	 * Store the given {@link XPath} object into the given {@link Document} as
	 * user data.
	 * </p>
	 * 
	 * <p>
	 * The {@link XPath} object can be retrieve using the method
	 * {@link #retrieveXPath(Document)}.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to store the {@link XPath} object in.
	 * @param xpath
	 *            is the {@link XPath} to store. Can be <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>.
	 */
	public static void storeXPath(Document d, XPath xpath) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		d.setUserData(XPATH, xpath, new CloneUserDataHandler());
	}

	/**
	 * <p>
	 * Retrieve from the given {@link Document}'s user data a previously stored
	 * {@link XPath} object.
	 * </p>
	 * 
	 * <p>
	 * An {@link XPath} object can be store using the method
	 * {@link #storeXPath(Document, XPath)}.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to retrieve the {@link XPath} object
	 *            from.
	 * 
	 * @return the previously stored {@link XPath} object, or <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>.
	 */
	public static XPath retrieveXPath(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		Object obj = d.getUserData(XPATH);
		if (obj != null) {
			return (XPath) obj;
		}
		return null;
	}

	private String msFFP = null;
	private Document moDOM = null;
	private XPath moXPath = XPathExpander.newXPath(null);

	public Doc() {
	}

	/**
	 * <p>
	 * Load the content of the file points by the given path into this object.
	 * </p>
	 * 
	 * @param path
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
	public void load(String path) throws MelodyException, IllegalDocException,
			IllegalFileException, IOException {
		setFileFullPath(path);
		try {
			setDocument(parse(new File(path)));
		} catch (SAXParseException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX_AT, new Object[] { path,
							Ex.getLineNumber(), Ex.getColumnNumber() }), Ex);
		} catch (SAXException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_SYNTAX, path), Ex);
		} catch (CharConversionException Ex) {
			throw new IllegalDocException(Messages.bind(
					Messages.DocEx_INVALID_XML_DATA, path), Ex);
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
					+ "Must be a valid " + Doc.class.getCanonicalName() + ".");
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
	 * @param xml
	 *            is an XML String.
	 * 
	 * @throws IllegalDocException
	 *             if the given XML content is not valid.
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	public void loadFromXML(String xml) throws MelodyException,
			IllegalDocException, IOException {
		try {
			setDocument(parse(xml));
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

	public String evaluateAsString(String expr) throws XPathExpressionException {
		return XPathExpander.evaluateAsString(expr, getDocument());
	}

	public NodeList evaluateAsNodeList(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNodeList(expr, getDocument());
	}

	public Node evaluateAsNode(String expr) throws XPathExpressionException {
		return XPathExpander.evaluateAsNode(expr, getDocument());
	}

	public void store() {
		if (getFileFullPath() == null) {
			return;
		}
		store(getFileFullPath());
	}

	public void store(String path) {
		if (getDocument() == null) {
			return;
		}
		store(getDocument(), path);
	}

	public String dump() {
		return dump(getDocument());
	}

	public String getFileFullPath() {
		return msFFP;
	}

	protected String setFileFullPath(String path) throws IllegalFileException {
		FS.validateFileExists(path);
		String previous = getFileFullPath();
		msFFP = path;
		return previous;
	}

	public Document getDocument() {
		return moDOM;
	}

	protected Document setDocument(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		Document previous = getDocument();
		moDOM = d;
		/*
		 * Store the XPath into the Document's user data, so it can be retrieved
		 * from everywhere.
		 */
		storeXPath(getDocument(), getXPath());
		return previous;
	}

	public XPath getXPath() {
		return moXPath;
	}

	/**
	 * if <tt>null</tt>, XPath Expression will be evaluated without custom
	 * namespace and custom xpath function support.
	 */
	public XPath setXPath(XPath xpath) {
		XPath previous = getXPath();
		moXPath = xpath;
		return previous;
	}

}

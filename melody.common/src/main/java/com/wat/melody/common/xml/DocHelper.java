package com.wat.melody.common.xml;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.location.Location;
import com.wat.melody.common.xml.location.LocationFactory;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DocHelper {

	private static DocumentBuilder _docBuilder;

	static {
		try {
			_docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a new Document Builder. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	protected static DocumentBuilder getDocumentBuilder() {
		return _docBuilder;
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
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		return parseNodeType(n.getNodeType());
	}

	public static String parseNodeType(int t) {
		switch (t) {
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
	 * Store the given {@link Document} into the given file.
	 * </p>
	 * 
	 * @param d
	 *            is the {@link Document} to store on disk.
	 * @param path
	 *            is a file path, which specifies where the given
	 *            {@link Document} will be stored.
	 * 
	 * @throws IllegalFileException
	 *             if the given path points to a directory.
	 * @throws IllegalFileException
	 *             if the given path points to a non readable file.
	 * @throws IllegalFileException
	 *             if the given path points to a non writable file.
	 * @throws IllegalDirectoryException
	 *             if the given file's parent directory is not a readable
	 *             directory.
	 * @throws IllegalDirectoryException
	 *             if the given file's parent directory is not a writable
	 *             directory.
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link Document} is <tt>null</tt>, or if the
	 *             given path is <tt>null</tt>.
	 */
	public static void store(Document d, String path)
			throws IllegalFileException, IllegalDirectoryException {
		FS.validateFilePath(path);
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
	 * @param d
	 *            is a {@link Document}.
	 * 
	 * @return the <tt>String</tt> representation of the given {@link Document}.
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
				String s = sw.toString();
				if (s.endsWith(SysTool.NEW_LINE)) {
					s = s.substring(0, s.length() - SysTool.NEW_LINE.length());
				}
				return s;
			}
		} catch (TransformerException Ex) {
			throw new RuntimeException("Error while dumping XML document.", Ex);
		}
	}

	/**
	 * @param e
	 *            is an {@link Element}.
	 * 
	 * @return an XPath Expression, which can be used to query the given
	 *         {@link Element} in the given {@link Element}'s owner
	 *         {@link Document}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getXPathPosition(Element e) {
		if (e == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		StringBuilder sTargetXPath = new StringBuilder();
		synchronized (e.getOwnerDocument()) {
			for (Node n = e; n.getParentNode() != null; n = n.getParentNode()) {
				sTargetXPath.insert(0, "[" + getChildNodePosition(n) + "]");
				sTargetXPath.insert(0, "/" + n.getNodeName());
			}
		}
		return sTargetXPath.toString();
	}

	private static int getChildNodePosition(Node e) {
		Node parent = e.getParentNode();
		int index = 1;
		for (int i = 0; i < parent.getChildNodes().getLength(); ++i) {
			Node c = parent.getChildNodes().item(i);
			if (c == e) {
				return index;
			} else if (c.getNodeName().equals(e.getNodeName())) {
				++index;
			}
		}

		throw new RuntimeException("Unexecpted error while looking "
				+ "for a child node position. "
				+ "The given node cannot be found in its parent chlids. "
				+ "Source code has certainly been modified and "
				+ "a bug have been introduced.");
	}

	public static Location getNodeLocation(Node n) {
		return LocationFactory.newLocation(n);
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

}
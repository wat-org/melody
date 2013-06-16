package com.wat.melody.common.xml;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Doc {

	private String _sourceFile = null;
	private Document _doc = null;
	private XPath _xPath;

	public Doc() {
		setXPath(XPathExpander.newXPath(null));
	}

	/**
	 * <p>
	 * Load the content of the file points by the given path into this object.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalFileException
	 *             if the given path doesn't point to a valid file (e.g. the
	 *             file doesn't exists, the path is a directory, ...).
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 */
	public synchronized void load(String path) throws MelodyException,
			IllegalDocException, IllegalFileException, IOException {
		File file = new File(path);
		setSourceFile(file.toString());
		try {
			setDocument(DocHelper.parse(file));
		} catch (SAXParseException Ex) {
			throw new IllegalDocException(Msg.bind(
					Messages.DocEx_INVALID_XML_SYNTAX_AT, path,
					Ex.getLineNumber(), Ex.getColumnNumber()), Ex);
		} catch (SAXException Ex) {
			throw new IllegalDocException(Msg.bind(
					Messages.DocEx_INVALID_XML_SYNTAX, path), Ex);
		} catch (CharConversionException Ex) {
			throw new IllegalDocException(Msg.bind(
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
	public synchronized void load(Doc doc) {
		if (doc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Doc.class.getCanonicalName() + ".");
		}
		try {
			if (doc.getSourceFile() != null) {
				setSourceFile(doc.getSourceFile());
			}
		} catch (IllegalFileException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "setting the Doc File Path to " + "'"
					+ doc.getSourceFile() + "'. "
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
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalDocException
	 *             if the given XML content is not valid.
	 */
	public synchronized void loadFromXML(String xml) throws MelodyException,
			IllegalDocException, IOException {
		try {
			setDocument(DocHelper.parse(xml));
		} catch (SAXParseException Ex) {
			throw new IllegalDocException(Msg.bind(
					Messages.DocEx_INVALID_XML_SYNTAX_AT, "inline content",
					Ex.getLineNumber(), Ex.getColumnNumber()), Ex);
		} catch (SAXException Ex) {
			throw new IllegalDocException(Msg.bind(
					Messages.DocEx_INVALID_XML_SYNTAX, "inline content"), Ex);
		} catch (CharConversionException Ex) {
			throw new IllegalDocException(Msg.bind(
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

	/**
	 * <p>
	 * Store this object in the file which was previously used to load it (see
	 * {@link #load(String)}).
	 * </p>
	 */
	public synchronized void store() {
		if (getSourceFile() == null) {
			return;
		}
		try {
			store(getSourceFile());
		} catch (IllegalFileException | IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "saving the Doc into its source File " + "'"
					+ getSourceFile() + "'. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. "
					+ "Or an external event made the file no more "
					+ "accessible (deleted, moved, read permission "
					+ "removed, ...).", Ex);
		}
	}

	/**
	 * @param path
	 *            is a file path, which specifies where the given
	 *            {@link Document} will be stored.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             {@inheritDoc}
	 * @throws IllegalDirectoryException
	 *             {@inheritDoc}
	 */
	public synchronized void store(String path) throws IllegalFileException,
			IllegalDirectoryException {
		if (getDocument() == null) {
			return;
		}
		DocHelper.store(getDocument(), path);
	}

	public synchronized String dump() {
		return DocHelper.dump(getDocument());
	}

	public String getSourceFile() {
		return _sourceFile;
	}

	protected String setSourceFile(String path) throws IllegalFileException {
		FS.validateFileExists(path);
		String previous = getSourceFile();
		_sourceFile = path;
		return previous;
	}

	public Document getDocument() {
		return _doc;
	}

	protected Document setDocument(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		Document previous = getDocument();
		_doc = d;
		/*
		 * Store the XPath into the Document's user data, so it can be retrieved
		 * from everywhere.
		 */
		DocHelper.storeXPath(_doc, getXPath());
		return previous;
	}

	public XPath getXPath() {
		return _xPath;
	}

	public XPath setXPath(XPath xpath) {
		if (xpath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + XPath.class.getCanonicalName() + ".");
		}
		XPath previous = getXPath();
		_xPath = xpath;
		return previous;
	}

}
package com.wat.melody.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * <p>
 * Parse a file and return a {@link Document}. Line number and column number are
 * added to each XML elements as user datas. The path of the source file is also
 * added to the returned {@link Document} as user data.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Parser {

	/**
	 * UserData key of each {@link Node}, which contains their line number.
	 */
	public static final String LINE_NUMBER = "l";

	/**
	 * UserData key of each {@link Node}, which contains their column number.
	 */
	public static final String COLUMN_NUMBER = "c";

	/**
	 * UserData key of the {@link Document}, which contains the source which was
	 * used to load it.
	 */
	public static final String SOURCE = "s";

	/**
	 * Global UserDataHandler, which makes copy of all UserDatas of each
	 * {@link Node} during IMPORT, CLONE, RENAME and ADOPT operation.
	 */
	public static final CloneUserDataHandler GenericCloneUserDataHandler = new CloneUserDataHandler();

	/**
	 * <p>
	 * Parse the given file and return a {@link Document}. Line number and
	 * column number are added to each XML Element {@link Node}s as user datas.
	 * The path of the given file is also added to the returned XML Element
	 * {@link Document} as user data.
	 * 
	 * <ul>
	 * <li>To get the line number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #LINE_NUMBER} key ;</li>
	 * <li>To get the column number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #COLUMN_NUMBER} key ;</li>
	 * <li>To get the file which was used to load the {@link Document}, call the
	 * {@link Node#getUserData(String)} on the {@link Document} object and query
	 * for {@link #SOURCE} key ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws SAXException
	 */
	public static Document parse(final File file) throws IOException,
			SAXException {
		if (file == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + File.class.getCanonicalName() + ".");
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			// track the file which was parsed
			Document doc = parse(new InputSource(is));
			trackSource(doc, file);
			return doc;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * <p>
	 * Parse the given String, which contains XML, and return a {@link Document}
	 * . Line number and column number are added to each XML Element
	 * {@link Node}s as user datas.
	 * 
	 * <ul>
	 * <li>To get the line number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #LINE_NUMBER} key ;</li>
	 * <li>To get the column number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #COLUMN_NUMBER} key ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws SAXException
	 */
	public static Document parse(final String content) throws IOException,
			SAXException {
		StringReader sr = null;
		try {
			sr = new StringReader(content);
			// track the content which was parsed
			Document doc = parse(new InputSource(sr));
			doc.setUserData(SOURCE, "input string",
					Parser.GenericCloneUserDataHandler);
			return doc;
		} finally {
			if (sr != null) {
				sr.close();
			}
		}
	}

	private static Document parse(final InputSource is) throws IOException,
			SAXException {
		MySAXHandler handler = new MySAXHandler();
		SAXParser parser = handler.getParser();
		parser.parse(is, handler);
		return handler.getDocument();
	}

	private static void trackSource(Document doc, File file) {
		Node n = doc.getFirstChild();
		if (n != null) {
			n.setUserData(SOURCE, file.toString(), GenericCloneUserDataHandler);
		}
	}

	protected static void trackLineNumber(Element e, int lineNumber) {
		e.setUserData(LINE_NUMBER, lineNumber, GenericCloneUserDataHandler);
	}

	protected static void trackColumnNumber(Element e, int colunmNumber) {
		e.setUserData(COLUMN_NUMBER, colunmNumber, GenericCloneUserDataHandler);
	}

}

class MySAXHandler extends DefaultHandler2 {

	public final static String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

	SAXParser parser;
	Document doc;
	Stack<Element> elementStack = new Stack<Element>();
	StringBuilder textBuffer = new StringBuilder();
	private Locator locator;

	public MySAXHandler() {
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException | SAXException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a new SAX Parser. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		try {
			parser.setProperty(LEXICAL_HANDLER_PROPERTY, this);
		} catch (SAXException Ex) {
			throw new RuntimeException("Unexecpted error while setting "
					+ "the lexical handler property to a SAX parser. "
					+ "Because this property is recognize and supported by "
					+ "the SAX parser, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		setParser(parser);
		setDocument(Doc.newDocument());
	}

	public SAXParser getParser() {
		return parser;
	}

	public SAXParser setParser(SAXParser p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SAXParser.class.getCanonicalName()
					+ ".");
		}
		SAXParser previous = getParser();
		parser = p;
		return previous;
	}

	public Document getDocument() {
		return doc;
	}

	public Document setDocument(Document d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Document.class.getCanonicalName()
					+ ".");
		}
		Document previous = getDocument();
		doc = d;
		return previous;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator; // Save the locator, so that it can be
								// used later for line tracking when
								// traversing nodes.
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		addTextIfNeeded();
		Element el = doc.createElement(qName);
		for (int i = 0; i < attributes.getLength(); i++) {
			el.setAttribute(attributes.getQName(i), attributes.getValue(i));
		}
		Parser.trackLineNumber(el, locator.getLineNumber());
		Parser.trackColumnNumber(el, locator.getColumnNumber());
		elementStack.push(el);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		addTextIfNeeded();
		Element closedEl = elementStack.pop();
		if (elementStack.isEmpty()) { // Is this the root element?
			doc.appendChild(closedEl);
		} else {
			Element parentEl = elementStack.peek();
			parentEl.appendChild(closedEl);
		}
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		textBuffer.append(ch, start, length);
	}

	// Outputs text accumulated under the current node
	private void addTextIfNeeded() {
		if (textBuffer.length() > 0) {
			Element el = elementStack.peek();
			Node textNode = doc.createTextNode(textBuffer.toString());
			el.appendChild(textNode);
			textBuffer.delete(0, textBuffer.length());
		}
	}

	@Override
	public void comment(char ch[], int start, int length) {
		addTextIfNeeded();
		Element el = elementStack.peek();
		Node textNode = doc.createComment(new String(ch, start, length));
		el.appendChild(textNode);
	}
}

class CloneUserDataHandler implements UserDataHandler {

	@Override
	public void handle(short op, String key, Object data, Node src, Node dst) {
		if (dst == null) {
			return;
		}
		dst.setUserData(key, data, this);
	}

}
package com.wat.melody.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

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

	public static final String LINE_NUMBER = "l";
	public static final String COLUMN_NUMBER = "c";
	public static final String FILE = "f";

	/**
	 * <p>
	 * Parse a file and return a {@link Document}. Line number and column number
	 * are added to each XML elements as user datas. The path of the source file
	 * is also added to the returned {@link Document} as user data.
	 * 
	 * <ul>
	 * <li>To get the line number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #LINE_NUMBER} ;</li>
	 * <li>To get the column number of a {@link Node}, call the
	 * {@link Node#getUserData(String)} on the {@link Node} object and query for
	 * {@link #COLUMN_NUMBER} ;</li>
	 * <li>To get the file which was used to load the {@link Document}, call the
	 * {@link Node#getUserData(String)} on the {@link Document} object and query
	 * for {@link #FILE} ;</li>
	 * </ul>
	 * </p>
	 */
	public static Document parse(final File sPath) throws IOException,
			SAXException {
		final Document doc = Doc.newDocument();
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a new Document Builder. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}

		final Stack<Element> elementStack = new Stack<Element>();
		final StringBuilder textBuffer = new StringBuilder();
		DefaultHandler handler = new DefaultHandler2() {
			private Locator locator;

			@Override
			public void setDocumentLocator(Locator locator) {
				this.locator = locator; // Save the locator, so that it can be
										// used later for line tracking when
										// traversing nodes.
			}

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				addTextIfNeeded();
				Element el = doc.createElement(qName);
				for (int i = 0; i < attributes.getLength(); i++) {
					el.setAttribute(attributes.getQName(i),
							attributes.getValue(i));
				}
				// track the line number
				el.setUserData(LINE_NUMBER, locator.getLineNumber(), null);
				// track the column number
				el.setUserData(COLUMN_NUMBER, locator.getColumnNumber(), null);
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
		};

		parser.setProperty("http://xml.org/sax/properties/lexical-handler",
				handler);

		InputStream is = null;
		try {
			is = new FileInputStream(sPath);
			parser.parse(is, handler);
			// track the file which was parsed
			doc.setUserData(FILE, sPath, null);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		return doc;
	}

}

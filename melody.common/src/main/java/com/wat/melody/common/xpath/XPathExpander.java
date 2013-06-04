package com.wat.melody.common.xpath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xpath.exception.XPathExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathExpander {

	/*
	 * TODO : XQuery doesn't support 'order by' and 'where'....
	 */

	static {
		// Specify we want the 'saxon XPath 2.0 resolver'
		System.setProperty("javax.xml.transform.TransformerFactory",
				"net.sf.saxon.TransformerFactoryImpl");
	}

	/**
	 * <p>
	 * Create a new {@link XPath} object, and assign it the given
	 * {@link XPathResolver}.
	 * </p>
	 * 
	 * @param xpathResolver
	 *            contains custom namespaces and custom functions definitions.
	 *            Can be <tt>null</tt>.
	 * 
	 * @return an {@link XPath} object, which can be used to evaluate XPath
	 *         Expression. When the given {@link XPathResolver} is <tt>null</tt>
	 *         , a default {@link XPath} object is returned, which doens't have
	 *         the capacity to evaluate any custom namespaces and custom
	 *         functions. When the given {@link XPathResolver} is not
	 *         <tt>null</tt>, the returned {@link XPath} object have the
	 *         capacity to evaluate custom namespace and functions defined in
	 *         the given {@link XPathResolver}.
	 */
	public static XPath newXPath(XPathResolver xpathResolver) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		if (xpathResolver != null) {
			xpath.setNamespaceContext(xpathResolver
					.getXPathNamespaceContextResolver());
			xpath.setXPathFunctionResolver(xpathResolver
					.getXPathFunctionResolver());
		}
		return xpath;
	}

	/**
	 * <p>
	 * Evaluate XPath expression as a <tt>String</tt>, in the given context
	 * (XPath 2.0 supported).
	 * </p>
	 * 
	 * <p>
	 * During the evaluation, the given context's owner {@link Document} is
	 * synchronized, preventing concurrent access to the owner {@link Document},
	 * which is not thread safe (because of xerces).
	 * </p>
	 * 
	 * <p>
	 * The given context {@link Node}'s owner {@link Document} can contain an
	 * {@link XPath} object (see {@link Doc#storeXPath(Document, XPath)} and
	 * {@link Doc#retrieveXPath(Document)}). If not <tt>null</tt>, this
	 * {@link XPath} object will perform the evaluation of the given XPath
	 * Expression. This {@link XPath} can contains custom namespaces and custom
	 * functions (see {@link XPath#setNamespaceContext(NamespaceContext)} and
	 * {@link XPath#setXPathFunctionResolver(XPathFunctionResolver)}), so that
	 * custom namespaces and custom functions used in the given XPath Expression
	 * will be evaluated successfully. If this {@link XPath} object is
	 * <tt>null</tt>, a default one will be used, which will not be able to
	 * evaluate custom namespaces and custom functions.
	 * </p>
	 * 
	 * @param expr
	 *            is the XPath 2.0 expression to evaluate.
	 * @param ctx
	 *            is the evaluation context (can be a {@link Document} or a
	 *            {@link Node}.
	 * 
	 * @return the evaluated expression, as a <tt>String</tt>.
	 * 
	 * @throws XPathExpressionException
	 *             if the given expression is not a valid XPath 2.0 expression.
	 * @throws NullPointerException
	 *             if the given expression is <tt>null</tt>.
	 */
	public static String evaluateAsString(String expr, Node ctx)
			throws XPathExpressionException {
		boolean isDoc = ctx.getNodeType() == Node.DOCUMENT_NODE;
		Document d = isDoc ? (Document) ctx : ctx.getOwnerDocument();
		XPath xpath = DocHelper.retrieveXPath(d);
		if (xpath == null) {
			xpath = newXPath(null);
		}
		synchronized (d) {
			return (String) xpath.evaluate(expr, ctx, XPathConstants.STRING);
		}
	}

	/**
	 * <p>
	 * Evaluate XPath expression as a {@link NodeList}, in the given context
	 * (XPath 2.0 supported).
	 * </p>
	 * 
	 * <p>
	 * During the evaluation, the given context's owner {@link Document} is
	 * synchronized, preventing concurrent access to the owner {@link Document},
	 * which is not thread safe (because of xerces).
	 * </p>
	 * 
	 * <p>
	 * The given context {@link Node}'s owner {@link Document} can contain an
	 * {@link XPath} object (see {@link Doc#storeXPath(Document, XPath)} and
	 * {@link Doc#retrieveXPath(Document)}). If not <tt>null</tt>, this
	 * {@link XPath} object will perform the evaluation of the given XPath
	 * Expression. This {@link XPath} can contains custom namespaces and custom
	 * functions (see {@link XPath#setNamespaceContext(NamespaceContext)} and
	 * {@link XPath#setXPathFunctionResolver(XPathFunctionResolver)}), so that
	 * custom namespaces and custom functions used in the given XPath Expression
	 * will be evaluated successfully. If this {@link XPath} object is
	 * <tt>null</tt>, a default one will be used, which will not be able to
	 * evaluate custom namespaces and custom functions.
	 * </p>
	 * 
	 * @param expr
	 *            is the XPath 2.0 expression to evaluate.
	 * @param ctx
	 *            is the evaluation context (can be a {@link Document} or a
	 *            {@link Node}.
	 * 
	 * @return the evaluated expression, as a {@link Node}.
	 * 
	 * @throws XPathExpressionException
	 *             if the given expression is not a valid XPath 2.0 expression.
	 * @throws NullPointerException
	 *             if the given expression is <tt>null</tt>.
	 */
	public static NodeList evaluateAsNodeList(String expr, Node ctx)
			throws XPathExpressionException {
		boolean isDoc = ctx.getNodeType() == Node.DOCUMENT_NODE;
		Document d = isDoc ? (Document) ctx : ctx.getOwnerDocument();
		XPath xpath = DocHelper.retrieveXPath(d);
		if (xpath == null) {
			xpath = newXPath(null);
		}
		synchronized (d) {
			return (NodeList) xpath.evaluate(expr, ctx, XPathConstants.NODESET);
		}
	}

	/**
	 * <p>
	 * Evaluate XPath expression as a {@link Node}, in the given context (XPath
	 * 2.0 supported).
	 * </p>
	 * 
	 * <p>
	 * During the evaluation, the given context's owner {@link Document} is
	 * synchronized, preventing concurrent access to the owner {@link Document},
	 * which is not thread safe (because of xerces).
	 * </p>
	 * 
	 * <p>
	 * The given context {@link Node}'s owner {@link Document} can contain an
	 * {@link XPath} object (see {@link Doc#storeXPath(Document, XPath)} and
	 * {@link Doc#retrieveXPath(Document)}). If not <tt>null</tt>, this
	 * {@link XPath} object will perform the evaluation of the given XPath
	 * Expression. This {@link XPath} can contains custom namespaces and custom
	 * functions (see {@link XPath#setNamespaceContext(NamespaceContext)} and
	 * {@link XPath#setXPathFunctionResolver(XPathFunctionResolver)}), so that
	 * custom namespaces and custom functions used in the given XPath Expression
	 * will be evaluated successfully. If this {@link XPath} object is
	 * <tt>null</tt>, a default one will be used, which will not be able to
	 * evaluate custom namespaces and custom functions.
	 * </p>
	 * 
	 * @param expr
	 *            is the XPath 2.0 expression to evaluate.
	 * @param ctx
	 *            is the evaluation context (can be a {@link Document} or a
	 *            {@link Node}.
	 * 
	 * @return the evaluated expression, as a {@link Node}.
	 * 
	 * @throws XPathExpressionException
	 *             if the given expression is not a valid XPath 2.0 expression.
	 * @throws NullPointerException
	 *             if the given expression is <tt>null</tt>.
	 */
	public static Node evaluateAsNode(String expr, Node ctx)
			throws XPathExpressionException {
		boolean isDoc = ctx.getNodeType() == Node.DOCUMENT_NODE;
		Document d = isDoc ? (Document) ctx : ctx.getOwnerDocument();
		XPath xpath = DocHelper.retrieveXPath(d);
		if (xpath == null) {
			xpath = newXPath(null);
		}
		synchronized (d) {
			return (Node) xpath.evaluate(expr, ctx, XPathConstants.NODE);
		}
	}

	public static final String DELIM_START = "§[";
	public static final String DELIM_STOP = "]§";

	/**
	 * <p>
	 * Expand all Melody Expressions found in the {@link File} which is pointed
	 * by the given {@link Path}.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Melody Expressions are delimited by '§[' and ']§' ;</li>
	 * <li>When a Melody Expression is equal to an XPath 2.0 Expression, it is
	 * replaced by its value (evaluated in the given context) ;</li>
	 * <li>When a Melody Expression is equal to a Property's Name, it is
	 * replaced by its value (found in the given properties) ;</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * During the evaluation, the given context's owner {@link Document} is
	 * synchronized, preventing concurrent access to the owner {@link Document},
	 * which is not thread safe (because of xerces).
	 * </p>
	 * 
	 * <p>
	 * The given context {@link Node}'s owner {@link Document} can contain an
	 * {@link XPath} object (see {@link Doc#storeXPath(Document, XPath)} and
	 * {@link Doc#retrieveXPath(Document)}). If not <tt>null</tt>, this
	 * {@link XPath} object will perform the evaluation of the given XPath
	 * Expression. This {@link XPath} can contains custom namespaces and custom
	 * functions (see {@link XPath#setNamespaceContext(NamespaceContext)} and
	 * {@link XPath#setXPathFunctionResolver(XPathFunctionResolver)}), so that
	 * custom namespaces and custom functions used in the given XPath Expression
	 * will be evaluated successfully. If this {@link XPath} object is
	 * <tt>null</tt>, a default one will be used, which will not be able to
	 * evaluate custom namespaces and custom functions.
	 * </p>
	 * 
	 * @param fileToExpand
	 *            is the {@link Path} of the {@link File} to expand.
	 * @param ctx
	 *            is the evaluation context (can be a {@link Document} or a
	 *            {@link Node}.
	 * @param properties
	 *            necessary to expand Property's Name.
	 * 
	 * @return the content of the file, as a <tt>String</tt>, where all Melody
	 *         Expressions have been expanded.
	 * 
	 * @throws XPathExpressionSyntaxException
	 *             if a Melody Expression cannot be expanded because it is not
	 *             valid.
	 * @throws IllegalFileException
	 *             if the given {@link Path} doesn't point to a valid
	 *             {@link File}.
	 * @throws IOException
	 *             if an IO error occurred while reading the {@link File} which
	 *             is pointed by the given {@link Path}.
	 * @throws IllegalArgumentException
	 *             if fileToExpand is <tt>null</tt>.
	 */
	public static String expand(Path fileToExpand, Node ctx,
			PropertySet properties) throws XPathExpressionSyntaxException,
			IOException, IllegalFileException {
		if (fileToExpand == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		FS.validateFileExists(fileToExpand.toString());
		String fileContent = new String(Files.readAllBytes(fileToExpand));
		try {
			return expand(fileContent, ctx, properties);
		} catch (XPathExpressionSyntaxException Ex) {
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_INVALID_XPATH_EXPR_IN_TEMPLATE,
					fileToExpand, fileContent.trim()), Ex);
		}
	}

	/**
	 * <p>
	 * Expand all Melody Expressions found in the given input
	 * <code>String</code>.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Melody Expressions are delimited by '§[' and ']§' ;</li>
	 * <li>When a Melody Expression is equal to an XPath 2.0 Expression, it is
	 * replaced by its value (evaluated in the given context) ;</li>
	 * <li>When a Melody Expression is equal to a Property's Name, it is
	 * replaced by its value (found in the given properties) ;</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * During the evaluation, the given context's owner {@link Document} is
	 * synchronized, preventing concurrent access to the owner {@link Document},
	 * which is not thread safe (because of xerces).
	 * </p>
	 * 
	 * <p>
	 * The given context {@link Node}'s owner {@link Document} can contain an
	 * {@link XPath} object (see {@link Doc#storeXPath(Document, XPath)} and
	 * {@link Doc#retrieveXPath(Document)}). If not <tt>null</tt>, this
	 * {@link XPath} object will perform the evaluation of the given XPath
	 * Expression. This {@link XPath} can contains custom namespaces and custom
	 * functions (see {@link XPath#setNamespaceContext(NamespaceContext)} and
	 * {@link XPath#setXPathFunctionResolver(XPathFunctionResolver)}), so that
	 * custom namespaces and custom functions used in the given XPath Expression
	 * will be evaluated successfully. If this {@link XPath} object is
	 * <tt>null</tt>, a default one will be used, which will not be able to
	 * evaluate custom namespaces and custom functions.
	 * </p>
	 * 
	 * @param expr
	 *            is the <code>String</code> to expand.
	 * @param ctx
	 *            necessary to expand XPath 2.0 Expression.
	 * @param vars
	 *            necessary to expand Property's Name.
	 * 
	 * @return the corresponding expanded <code>String</code>.
	 * 
	 * @throws XPathExpressionSyntaxException
	 *             if a Melody Expression cannot be expanded because it is not a
	 *             valid X2Path Expression.
	 */
	public static String expand(String expr, Node ctx, PropertySet vars)
			throws XPathExpressionSyntaxException {
		if (expr == null) {
			return null;
		}
		int start = expr.indexOf(DELIM_START);
		int end = expr.indexOf(DELIM_STOP);
		if (start == -1) {
			// Start Delimiter not found
			if (end == -1) {
				// Start Delimiter not found AND Stop Delimiter not found
				return expr;
			} else {
				// Start Delimiter not found AND Stop Delimiter found
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_START_DELIM_MISSING,
						extractPart(expr, end)));
			}
		}
		// Start Delimiter found
		if (end == -1) {
			// Start Delimiter found AND Stop Delimiter not found
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_STOP_DELIM_MISSING,
					extractPart(expr, start)));
		} else if (end < start) {
			// Start Delimiter found AFTER Stop Delimiter
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_START_DELIM_MISSING,
					extractPart(expr, end)));
		}
		int next = start;
		while ((next = expr.indexOf(DELIM_START, next + DELIM_START.length())) != -1) {
			if (next < end) {
				end = expr.indexOf(DELIM_STOP, end + DELIM_STOP.length());
			} else {
				break;
			}

		}
		if (end == -1) {
			// Start Delimiter found AND Stop Delimiter not found
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_STOP_DELIM_MISSING,
					extractPart(expr, start)));
		}
		StringBuilder str = new StringBuilder();
		str.append(expr.substring(0, start));
		str.append(resolvedXPathExpression(
				expr.substring(start + DELIM_START.length(), end), ctx, vars));
		str.append(expand(expr.substring(end + DELIM_STOP.length()), ctx, vars));
		return str.toString();
	}

	private static String resolvedXPathExpression(String expr, Node ctx,
			PropertySet vars) throws XPathExpressionSyntaxException {
		// Expand Nested Expression
		expr = expand(expr, ctx, vars);
		expr = expr.trim();
		// Here, all Nested Expression have been expanded
		if (expr.matches("^" + PropertyName.PATTERN + "$")) {
			// If it matches the PropertyName Pattern, the Expression is
			// remplaced by the Property's value
			if (vars == null) {
				throw new RuntimeException("Cannot expand the property '"
						+ expr
						+ "' because no PropertiesSet have been provided.");
			}
			if (vars.containsKey(expr)) {
				return vars.get(expr);
			} else {
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_UNDEF_PROPERTY, expr));
			}
		} else {
			if (ctx == null) {
				throw new RuntimeException("Cannot expand the expression '"
						+ expr + "' because no Context have been provided.");
			}
			try {
				return evaluateAsString(expr, ctx);
			} catch (XPathExpressionException Ex) {
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_INVALID_XPATH_EXPR,
						extractPart(expr, 0)), Ex);
			}
		}
	}

	private static String extractPart(String sMsg, int near) {
		int relLeft = 100;
		int relRight = 100;
		if (sMsg == null) {
			throw new IllegalArgumentException(relLeft + ": Not accepted. "
					+ "Must be a valid String.");
		}
		if (near < 0) {
			throw new IllegalArgumentException(relLeft + ": Not accepted. "
					+ "Must be a positive integer.");
		}
		String left = "..... truncated datas .....\n";
		String right = "\n...........................";
		int absLeft = near - relLeft;
		int absRight = near + relRight;
		if (absLeft > sMsg.length()) {
			absLeft = sMsg.length() - (relLeft + relRight);
			absRight = sMsg.length();
		}
		if (absLeft <= 0) {
			absLeft = 0;
			left = "----- full datas ----\n";
			absRight = relLeft + relRight;
		}
		if (absRight >= sMsg.length()) {
			absRight = sMsg.length();
			right = "\n---------------------";
			absLeft = sMsg.length() - (relLeft + relRight);
		}
		if (absLeft <= 0) {
			absLeft = 0;
			left = "----- full datas ----\n";
		}

		return left + sMsg.substring(absLeft, absRight).trim() + right;
	}
}
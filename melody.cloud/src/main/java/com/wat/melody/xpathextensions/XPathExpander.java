package com.wat.melody.xpathextensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.exception.XPathExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathExpander {

	static {
		CustomXPathFunctions.load();
	}

	public static final String DELIM_START = "§[";
	public static final String DELIM_STOP = "]§";

	/**
	 * <p>
	 * Expand all Melody X2Path Expressions found in the {@link File} which is
	 * pointed by the given input {@link Path}.
	 * </p>
	 * 
	 * <p>
	 * <i> * Melody X2Path Expressions are delimited by '§[' and ']§' ; <BR/>
	 * * When a Melody X2Path Expression is equal to an XPath 2.0 Expression, it
	 * is replaced by its value (found in the given {@link Node} context) ; <BR/>
	 * * When a Melody X2Path Expression is equal to a Property's Name (declared
	 * in the given {@link PropertiesSet}), it is replaced by its value (found
	 * in the given {@link PropertiesSet}) ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param fileToExpand
	 *            is the {@link File} which is pointed by the given input
	 *            {@link Path}.
	 * @param context
	 *            necessary to expand XPath 2.0 Expression.
	 * @param vars
	 *            necessary to expand Property's Name.
	 * 
	 * @return the corresponding expanded <code>String</code>.
	 * 
	 * @throws XPathExpressionSyntaxException
	 *             if a Melody Expression cannot be expanded because it is not a
	 *             valid X2Path Expression.
	 * @throws IllegalFileException
	 *             if the given {@link Path} doesn't point to a valid
	 *             {@link File}.
	 * @throws IOException
	 *             if an IO error occurred while reading the {@link File} which
	 *             is pointed by the given input {@link Path}.
	 * @throws IllegalArgumentException
	 *             if fileToExpand is <code>null</code>.
	 */
	public static String expand(Path fileToExpand, Node context,
			PropertiesSet vars) throws XPathExpressionSyntaxException,
			IOException, IllegalFileException {
		if (fileToExpand == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Path (a File Path).");
		}
		FS.validateFileExists(fileToExpand.toString());
		String fileContent = new String(Files.readAllBytes(fileToExpand));
		try {
			return expand(fileContent, context, vars);
		} catch (XPathExpressionSyntaxException Ex) {
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_INVALID_XPATH_EXPR_IN_TEMPLATE,
					fileToExpand, fileContent.trim()), Ex);
		}
	}

	/**
	 * <p>
	 * Expand all Melody X2Path Expressions found in the given input
	 * <code>String</code>.
	 * </p>
	 * 
	 * <p>
	 * <i> * Melody X2Path Expressions are delimited by '§[' and ']§' ; <BR/>
	 * * When a Melody X2Path Expression is equal to an XPath 2.0 Expression, it
	 * is replaced by its value (found in the given {@link Node} context) ; <BR/>
	 * * When a Melody X2Path Expression is equal to a Property's Name (declared
	 * in the given {@link PropertiesSet}), it is replaced by its value (found
	 * in the given {@link PropertiesSet}) ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sBase
	 *            is the <code>String</code> to expand.
	 * @param context
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
	public static String expand(String sBase, Node context, PropertiesSet vars)
			throws XPathExpressionSyntaxException {
		if (sBase == null) {
			return null;
		}
		int start = sBase.indexOf(DELIM_START);
		int end = sBase.indexOf(DELIM_STOP);
		if (start == -1) {
			// Start Delimiter not found
			if (end == -1) {
				// Start Delimiter not found AND Stop Delimiter not found
				return sBase;
			} else {
				// Start Delimiter not found AND Stop Delimiter found
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_START_DELIM_MISSING,
						extractPart(sBase, end)));
			}
		}
		// Start Delimiter found
		if (end == -1) {
			// Start Delimiter found AND Stop Delimiter not found
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_STOP_DELIM_MISSING,
					extractPart(sBase, start)));
		} else if (end < start) {
			// Start Delimiter found AFTER Stop Delimiter
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_START_DELIM_MISSING,
					extractPart(sBase, end)));
		}
		int next = start;
		while ((next = sBase.indexOf(DELIM_START, next + DELIM_START.length())) != -1) {
			if (next < end) {
				end = sBase.indexOf(DELIM_STOP, end + DELIM_STOP.length());
			} else {
				break;
			}

		}
		if (end == -1) {
			// Start Delimiter found AND Stop Delimiter not found
			throw new XPathExpressionSyntaxException(Messages.bind(
					Messages.XPathExprSyntaxEx_STOP_DELIM_MISSING,
					extractPart(sBase, start)));
		}
		return sBase.substring(0, start)
				+ resolvedXPathExpression(
						sBase.substring(start + DELIM_START.length(), end),
						context, vars)
				+ expand(sBase.substring(end + DELIM_STOP.length()), context,
						vars);
	}

	/**
	 * <p>
	 * Look for given attribute in the Node and its herited parents. Return the
	 * value of the first attribute found, where all XPath Expression have been
	 * expanded.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Node} and its herited parents.
	 * 
	 * @return the value of the first attribute found, where all XPath
	 *         Expression have been expanded.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression).
	 */
	public static String getHeritedAttributeValue(Node n, String sAttrName)
			throws ResourcesDescriptorException {
		return getHeritedAttributeValue(n, sAttrName, true);
	}

	/**
	 * <p>
	 * Look for given attribute in the Node and its herited parents. Return the
	 * value of the first attribute found. The returned value is expanded or
	 * not, regarding the value of the third boolean argument.
	 * </p>
	 * 
	 * @param n
	 *            is the {@link Node} to search in.
	 * @param sAttrName
	 *            is the name of the attribute to found in the given
	 *            {@link Node} and its herited parents.
	 * 
	 * @return the value of the first attribute found. The returned value is
	 *         expanded or not, regarding the value of the third boolean
	 *         argument.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given attribute's value cannot be expanded (e.g.
	 *             contains an invalid XPath Expression). This should not
	 *             happened if the third boolean argument is <tt>false</tt>.
	 */
	public static String getHeritedAttributeValue(Node n, String sAttrName,
			boolean expand) throws ResourcesDescriptorException {
		Node attr = FilteredDocHelper.getHeritedAttribute(n, sAttrName);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (!expand || v == null) {
			return v;
		}
		try {
			return XPathExpander.expand(v,
					n.getOwnerDocument().getFirstChild(), null);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private static String resolvedXPathExpression(String sXPathExpr,
			Node context, PropertiesSet vars)
			throws XPathExpressionSyntaxException {
		// Expand Nested Expression
		sXPathExpr = expand(sXPathExpr, context, vars);
		sXPathExpr = sXPathExpr.trim();
		// Here, all Nested Expression have been expanded
		if (sXPathExpr.matches("^" + PropertyName.PATTERN + "$")) {
			// If it matches the PropertyName Pattern, the Expression is
			// remplaced by the Property's value
			if (vars == null) {
				throw new RuntimeException("Cannot expand the property '"
						+ sXPathExpr
						+ "' because no PropertiesSet have been provided.");
			}
			if (vars.containsKey(sXPathExpr)) {
				return vars.get(sXPathExpr);
			} else {
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_UNDEF_PROPERTY, sXPathExpr));
			}
		} else {
			if (context == null) {
				throw new RuntimeException("Cannot expand the expression '"
						+ sXPathExpr
						+ "' because no Context have been provided.");
			}
			try {
				return Doc.evaluateAsString(sXPathExpr, context);
			} catch (XPathExpressionException Ex) {
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_INVALID_XPATH_EXPR,
						extractPart(sXPathExpr, 0)), Ex);
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
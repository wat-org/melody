package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.xpath.XPathExpressionException;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.Messages;
import com.wat.melody.api.exception.XPathExpressionSyntaxException;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.PropertyName;
import com.wat.melody.xpathextensions.CustomXPathFunctions;

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
	 * is replaced by the XPath 2.0 Expression's resolution in the
	 * {@link IResourcesDescriptor} ; <BR/>
	 * * When a Melody X2Path Expression is equal to a Property's Name (declared
	 * in the given {@link PropertiesSet}), it is replaced by its value (found
	 * in the given {@link PropertiesSet}) ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param fileToExpand
	 *            is the {@link File} which is pointed by the given input
	 *            {@link Path}.
	 * @param pm
	 *            necessary to expand XPath 2.0 Expression.
	 * @param vars
	 *            necessary to expand Property's Name.
	 * 
	 * @return the corresponding expanded <code>String</code>.
	 * 
	 * @throws XPathExpressionSyntaxException
	 *             if a Melody Expression cannot be expanded because it is not a
	 *             valid X2Path Expression.
	 * @throws IOException
	 *             if an IO error occurred while reading the {@link File} which
	 *             is pointed by the given input {@link Path}.
	 */
	public static String expand(Path fileToExpand, IProcessorManager pm,
			PropertiesSet vars) throws XPathExpressionSyntaxException,
			IOException {
		String fileContent = new String(Files.readAllBytes(fileToExpand));
		try {
			return expand(fileContent, pm, vars);
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
	 * is replaced by the XPath 2.0 Expression's resolution in the
	 * {@link IResourcesDescriptor} ; <BR/>
	 * * When a Melody X2Path Expression is equal to a Property's Name (declared
	 * in the given {@link PropertiesSet}), it is replaced by its value (found
	 * in the given {@link PropertiesSet}) ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sBase
	 *            is the <code>String</code> to expand.
	 * @param pm
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
	public static String expand(String sBase, IProcessorManager pm,
			PropertiesSet vars) throws XPathExpressionSyntaxException {
		int start = sBase.indexOf(DELIM_START);
		if (start == -1) {
			// Start Delimiter not found
			if (sBase.indexOf(DELIM_STOP) == -1) {
				// Start Delimiter not found AND Stop Delimiter not found
				return sBase;
			} else {
				// Start Delimiter not found AND Stop Delimiter found
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_START_DELIM_MISSING,
						extractPart(sBase, sBase.indexOf(DELIM_STOP))));
			}
		}
		// Search for the Stop Delimiter
		int end = sBase.indexOf(DELIM_STOP);
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
						sBase.substring(start + DELIM_START.length(), end), pm,
						vars)
				+ expand(sBase.substring(end + DELIM_STOP.length()), pm, vars);
	}

	private static String resolvedXPathExpression(String sXPathExpr,
			IProcessorManager pm, PropertiesSet vars)
			throws XPathExpressionSyntaxException {
		// Expand Nested Expression
		sXPathExpr = expand(sXPathExpr, pm, vars);
		sXPathExpr = sXPathExpr.trim();
		// Here, all Nested Expression have been expanded
		if (sXPathExpr.matches("^" + PropertyName.PATTERN + "$")) {
			// If it matches the PropertyName Pattern, the Expression is
			// remplaced by the Property's value
			if (vars.containsKey(sXPathExpr)) {
				return vars.get(sXPathExpr);
			} else {
				throw new XPathExpressionSyntaxException(Messages.bind(
						Messages.XPathExprSyntaxEx_UNDEF_PROPERTY,
						extractPart(sXPathExpr, 0)));
			}
		} else {
			try {
				return pm.getResourcesDescriptor().evaluateAsString(sXPathExpr);
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
		String left = "\n.....\n";
		String right = "\n.....\n";
		int absLeft = near - relLeft;
		int absRight = near + relRight;
		if (absLeft > sMsg.length()) {
			absLeft = sMsg.length() - (relLeft + relRight);
			absRight = sMsg.length();
		}
		if (absLeft <= 0) {
			absLeft = 0;
			left = "\n-----\n";
			absRight = relLeft + relRight;
		}
		if (absRight >= sMsg.length()) {
			absRight = sMsg.length();
			right = "\n-----\n";
			absLeft = sMsg.length() - (relLeft + relRight);
		}
		if (absLeft <= 0) {
			absLeft = 0;
			left = "\n-----\n";
		}

		return left + sMsg.substring(absLeft, absRight).trim() + right;
	}
}
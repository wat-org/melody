package com.wat.melody.common.xpath;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.xpath.messages";

	public static String XPathResolver_INVALID_NAMESPACE_DEFINITION;
	public static String XPathResolver_INVALID_NAMESPACE_NAME;

	public static String XPathResolver_INVALID_NAMESPACE_URI_DEFINITION;
	public static String XPathResolver_MISSING_NAMESPACE_URI;
	public static String XPathResolver_INVALID_NAMESPACE_URI;

	public static String XPathResolver_INVALID_FUNCTION_DEFINITION;
	public static String XPathResolver_INVALID_FUNCTION_NAME_DEFINITION;
	public static String XPathResolver_INVALID_FUNCTION_NAME;

	public static String XPathResolver_INVALID_FUNCTION_NAMESPACE_URI_DEFINITION;
	public static String XPathResolver_MISSING_FUNCTION_NAMESPACE_URI;
	public static String XPathResolver_INVALID_FUNCTION_NAMESPACE_URI;

	public static String XPathResolver_INVALID_FUNCTION_CLASS_DEFINITION;
	public static String XPathResolver_MISSING_FUNCTION_CLASS;
	public static String XPathResolver_INVALID_FUNCTION_CLASS_EMPTY;
	public static String XPathResolver_INVALID_FUNCTION_CLASS_CNF;
	public static String XPathResolver_INVALID_FUNCTION_CLASS_NCDF;
	public static String XPathResolver_INVALID_FUNCTION_CLASS_CC;
	public static String XPathResolver_INVALID_FUNCTION_CLASS_IA;

	public static String XPathResolver_INVALID_FUNCTION_ARITY_DEFINITION;
	public static String XPathResolver_MISSING_FUNCTION_ARITY;
	public static String XPathResolver_INVALID_FUNCTION_ARITY;

	public static String XPathExprSyntaxEx_UNDEF_PROPERTY;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR_IN_TEMPLATE;
	public static String XPathExprSyntaxEx_START_DELIM_MISSING;
	public static String XPathExprSyntaxEx_STOP_DELIM_MISSING;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
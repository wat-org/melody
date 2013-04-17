package com.wat.melody.common.xpath;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.xpath.messages";

	public static String XPathResolver_INVALID_NAMESPACE_DEFINITION;
	public static String XPathResolver_INVALID_NAMESPACE_NAME;

	public static String XPathResolver_INVALID_NAMESPACE_URI_DEFINITION;
	public static String XPathResolver_MISSING_NAMESPACE_URI;
	public static String XPathResolver_INVALID_NAMESPACE_URI;

	public static String XPathExprSyntaxEx_UNDEF_PROPERTY;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR_IN_TEMPLATE;
	public static String XPathExprSyntaxEx_START_DELIM_MISSING;
	public static String XPathExprSyntaxEx_STOP_DELIM_MISSING;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String bind(String message, Object... bindings) {
		return NLS.bind(message, bindings);
	}

	private Messages() {
	}

}
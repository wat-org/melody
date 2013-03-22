package com.wat.melody.xpathextensions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.xpathextensions.messages";

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

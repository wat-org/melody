package com.wat.melody.xpathextensions.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.xpathextensions.common.messages";

	public static String XPathExprSyntaxEx_UNDEF_PROPERTY;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR;
	public static String XPathExprSyntaxEx_INVALID_XPATH_EXPR_IN_TEMPLATE;
	public static String XPathExprSyntaxEx_START_DELIM_MISSING;
	public static String XPathExprSyntaxEx_STOP_DELIM_MISSING;

	public static String RDEx_INVALID_HERIT_ATTR_XPATH;
	public static String RDEx_INVALID_HERIT_ATTR_MANYNODEMATCH;
	public static String RDEx_INVALID_HERIT_ATTR_NONODEMATCH;
	public static String RDEx_INVALID_HERIT_ATTR_CIRCULARREF;

	public static String NetMgmtMethodEx_EMPTY;
	public static String NetMgmtMethodEx_INVALID;

	public static String NetMgmtMsg_TOO_MANY_MGMT_NODE;
	public static String NetMgmtEx_NO_MGMT_NODE;
	public static String NetMgmtEx_MISSING_ATTR;
	public static String NetMgmtEx_INVALID_ATTR;
	public static String NetMgmtEx_TOO_MANY_MGMT_NETWORK_INTERFACE;
	public static String NetMgmtEx_NO_MGMT_NETWORK_INTERFACE;
	public static String NetMgmtEx_INVALID_MGMT_NETWORK_INTERFACE_SELECTOR;
	public static String NetMgmtEx_INVALID_MGMT_NETWORK_INTERFACE_ATTRIBUTE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

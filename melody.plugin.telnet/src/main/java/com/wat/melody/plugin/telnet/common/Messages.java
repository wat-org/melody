package com.wat.melody.plugin.telnet.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.telnet.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;

	public static String TelnetEx_AUTH_FAIL;
	public static String TelnetEx_VALIDATION_ERR;
	public static String TelnetEx_MISSING_CMD;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
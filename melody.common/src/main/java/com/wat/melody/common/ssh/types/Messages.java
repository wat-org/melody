package com.wat.melody.common.ssh.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.typedef.messages";

	public static String ModifiersEx_EMPTY;
	public static String ModifiersEx_INVALID;

	public static String LinkOptionEx_EMPTY;
	public static String LinkOptionEx_INVALID;

	public static String GroupIDEx_EMPTY;
	public static String GroupIDEx_INVALID;

	public static String ResourceEx_INVALID_MATCH_ATTR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
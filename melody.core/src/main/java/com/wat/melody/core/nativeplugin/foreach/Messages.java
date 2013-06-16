package com.wat.melody.core.nativeplugin.foreach;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.foreach.messages";

	public static String ForeachEx_EMPTY_ITEMS_ATTR;
	public static String ForeachEx_INVALID_ITEMS_ATTR;
	public static String ForeachEx_INVALID_MAXPAR_ATTR;

	public static String ForeachEx_INTERRUPTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
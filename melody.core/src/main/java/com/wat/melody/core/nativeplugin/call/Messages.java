package com.wat.melody.core.nativeplugin.call;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.call.messages";

	public static String CallEx_MISSING_REF;
	public static String CallEx_MISSING_ORDERS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
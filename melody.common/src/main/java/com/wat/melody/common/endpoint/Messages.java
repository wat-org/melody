package com.wat.melody.common.endpoint;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.endpoint.messages";

	public static String ContextRootEx_EMPTY;
	public static String ContextRootEx_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

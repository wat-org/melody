package com.wat.melody.common.timeout;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.timeout.messages";

	public static String TimeoutEx_EMPTY;
	public static String TimeoutEx_NOT_A_NUMBER;
	public static String TimeoutEx_NEGATIVE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
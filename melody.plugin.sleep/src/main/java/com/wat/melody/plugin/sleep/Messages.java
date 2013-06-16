package com.wat.melody.plugin.sleep;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.sleep.messages";

	public static String SleepTimeoutEx_INVALID;
	public static String SleepMsg_INFO;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
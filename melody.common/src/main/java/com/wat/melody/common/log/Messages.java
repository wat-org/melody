package com.wat.melody.common.log;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.log.messages";

	public static String LogThresholdEx_EMPTY_STRSTR;
	public static String LogThresholdEx_INVALID_STRSTR;
	public static String LogThresholdEx_MAX_REACHED;
	public static String LogThresholdEx_MIN_REACHED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
package com.wat.melody.cloud;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.messages";

	public static String InstanceTypeEx_EMPTY;
	public static String InstanceTypeEx_INVALID;

	public static String InstanceStateEx_EMPTY;
	public static String InstanceStateEx_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

package com.wat.melody.plugin.copy.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.copy.common.messages";

	public static String CopyEx_INVALID_MAXPAR_ATTR;
	public static String CopyEx_READ_IO_ERROR;
	public static String CopyEx_WRITE_IO_ERROR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
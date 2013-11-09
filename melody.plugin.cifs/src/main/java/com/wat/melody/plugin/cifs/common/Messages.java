package com.wat.melody.plugin.cifs.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.cifs.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;

	public static String TransferEx_INVALID_MAXPAR_ATTR;
	public static String TransferEx_READ_IO_ERROR;
	public static String TransferEx_WRITE_IO_ERROR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
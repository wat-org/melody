package com.wat.melody.core.nativeplugin.sequence;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.sequence.messages";

	public static String SequenceEx_IO_ERROR;
	public static String SequenceEx_INTERRUPTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

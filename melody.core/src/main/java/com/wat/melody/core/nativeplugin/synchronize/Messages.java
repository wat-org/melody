package com.wat.melody.core.nativeplugin.synchronize;

import org.eclipse.osgi.util.NLS;

public abstract class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.synchronize.messages";

	public static String LockIdEx_EMPTY;
	public static String LockIdEx_INVALID;

	public static String MaxParEx_EMPTY;
	public static String MaxParEx_NOT_A_NUMBER;
	public static String MaxParEx_NEGATIVE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

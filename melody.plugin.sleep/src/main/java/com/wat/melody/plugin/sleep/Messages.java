package com.wat.melody.plugin.sleep;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.sleep.messages";

	public static String SleepEx_INVALID_MILLIS_ATTR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

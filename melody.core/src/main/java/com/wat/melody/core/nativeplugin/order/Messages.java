package com.wat.melody.core.nativeplugin.order;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.order.messages";

	public static String OrderEx_DUPLICATE_NAME;

	public static String OrderEx_INTERRUPTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

package com.wat.melody.xpath;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.xpath.messages";

	public static String RDEx_INVALID_HERIT_ATTR_XPATH;
	public static String RDEx_INVALID_HERIT_ATTR_MANYNODEMATCH;
	public static String RDEx_INVALID_HERIT_ATTR_NONODEMATCH;
	public static String RDEx_INVALID_HERIT_ATTR_CIRCULARREF;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

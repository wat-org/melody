package com.wat.melody.common.order;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.order.messages";

	public static String OrderNameSetEx_EMPTY_ORDER_NAME;
	public static String OrderNameSetEx_INVALID_ORDER_NAME;
	public static String OrderNameSetEx_EMPTY;

	public static String OrderNameEx_EMPTY;
	public static String OrderNameEx_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

package com.wat.cloud.libvirt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.libvirt.messages";

	public static String KeyPairEx_DIFFERENT;

	public static String StartEx_TIMEOUT;

	public static String StopEx_TIMEOUT;

	public static String ResizeEx_FAILED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String bind(String message, Object... bindings) {
		return NLS.bind(message, bindings);
	}

	private Messages() {
	}

}

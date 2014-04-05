package com.wat.cloud.libvirt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.libvirt.messages";

	public static String KeyPairEx_DIFFERENT;

	public static String StartEx_TIMEOUT;

	public static String StopEx_TIMEOUT;

	public static String ResizeEx_FAILED;

	public static String PADestroyEx_STILL_IN_USE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
package com.wat.melody.cloud.disk;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.disk.messages";

	public static String DiskEx_EMPTY_SIZE_ATTR;
	public static String DiskEx_INVALID_SIZE_ATTR;
	public static String DiskEx_EMPTY_DEVICE_ATTR;
	public static String DiskEx_INVALID_DEVICE_ATTR;

	public static String DiskListEx_DEVICE_ALREADY_DEFINE;
	public static String DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE;

	public static String DiskLoadEx_MISSING_ATTR;

	public static String DiskDefEx_UNDEF_ROOT_DEVICE;
	public static String DiskDefEx_INCORRECT_ROOT_DEVICE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

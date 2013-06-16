package com.wat.melody.cloud.disk;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.disk.messages";

	public static String DiskListEx_DEVICE_ALREADY_DEFINE;
	public static String DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE;

	public static String DiskDevLoaderEx_MISSING_ATTR;
	public static String DiskDevLoaderEx_GENERIC_ERROR;

	public static String DiskDefEx_EMPTY_DEVICE_LIST;
	public static String DiskDefEx_UNDEF_ROOT_DEVICE;
	public static String DiskDefEx_INCORRECT_ROOT_DEVICE;

	public static String DiskDeviceSizeEx_EMPTY_SIZE;
	public static String DiskDeviceSizeEx_NEGATIVE_SIZE;
	public static String DiskDeviceSizeEx_INVALID_SIZE;

	public static String DiskDeviceNameEx_EMPTY;
	public static String DiskDeviceNameEx_INVALID;

	public static String DiskMgmtEx_SELECTOR_INVALID_XPATH;
	public static String DiskMgmtEx_SELECTOR_NOT_MATCH_ELMT;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
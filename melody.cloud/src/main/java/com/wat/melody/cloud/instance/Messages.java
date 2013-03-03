package com.wat.melody.cloud.instance;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.instance.messages";

	public static String InstanceTypeEx_EMPTY;
	public static String InstanceTypeEx_INVALID;

	public static String InstanceStateEx_EMPTY;
	public static String InstanceStateEx_INVALID;

	public static String UpdateDiskDevEx_IMPOSSIBLE;
	public static String UpdateDiskDevMsg_DISK_DEVICES_RESUME;

	public static String UpdateNetDevMsg_NETWORK_DEVICES_RESUME;

	public static String IngressMsg_FWRULES_RESUME;

	public static String InstanceEx_MANAGEMENT_ENABLE_FAILED;
	public static String InstanceEx_MANAGEMENT_DISABLE_FAILED;
	public static String InstanceMsg_MANAGEMENT_ENABLE_BEGIN;
	public static String InstanceMsg_MANAGEMENT_ENABLE_SUCCESS;
	public static String InstanceMsg_MANAGEMENT_DISABLE_BEGIN;
	public static String InstanceMsg_MANAGEMENT_DISABLE_SUCCESS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

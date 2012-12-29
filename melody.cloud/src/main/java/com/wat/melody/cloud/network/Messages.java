package com.wat.melody.cloud.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.network.messages";

	public static String NetworkEx_EMPTY_DEVICE_NAME;
	public static String NetworkEx_INVALID_DEVICE_NAME;

	public static String NetworkListEx_DEVICE_ALREADY_DEFINE;

	public static String NetworkLoadEx_MISSING_ATTR;

	public static String NetMgmtMsg_INTRO;
	public static String NetMgmtMsg_RESUME;
	public static String NetMgmtMsg_FAILED;

	public static String NetMgmtEx_SSH_MGMT_ENABLE_TIMEOUT;
	public static String NetMgmtMsg_SSH_WAIT_FOR_MGMT_ENABLE;

	public static String NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

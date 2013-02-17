package com.wat.melody.cloud.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.network.messages";

	public static String NetMgmtMethodEx_EMPTY;
	public static String NetMgmtMethodEx_INVALID;

	public static String NetworkDeviceNameEx_EMPTY;
	public static String NetworkDeviceNameEx_INVALID;

	public static String NetworkListEx_DEVICE_ALREADY_DEFINE;

	public static String NetworkLoadEx_MISSING_ATTR;

	public static String NetMgmtMsg_INTRO;
	public static String NetMgmtMsg_RESUME;
	public static String NetMgmtMsg_FAILED;

	public static String NetMgmtEx_SSH_MGMT_ENABLE_TIMEOUT;
	public static String NetMgmtMsg_SSH_WAIT_FOR_MGMT_ENABLE;

	public static String NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED;

	public static String NetMgmtEx_NO_MGMT_NODE;
	public static String NetMgmtEx_MISSING_ATTR;
	public static String NetMgmtEx_INVALID_ATTR;
	public static String NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE;
	public static String NetMgmtEx_NO_MGMT_NETWORK_DEVICE;
	public static String NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_SELECTOR;
	public static String NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_ATTRIBUTE;
	public static String NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

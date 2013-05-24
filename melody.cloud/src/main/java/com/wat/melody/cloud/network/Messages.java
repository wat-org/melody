package com.wat.melody.cloud.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.network.messages";

	public static String MgmtNetworkEnableTimeoutEx_INVALID;

	public static String MgmtNetworkMethodEx_EMPTY;
	public static String MgmtNetworkMethodEx_INVALID;

	public static String NetworkDeviceNameListEx_DEVICE_ALREADY_DEFINE;

	public static String NetworkDevLoaderEx_MISSING_ATTR;
	public static String NetworkDevLoaderEx_GENERIC_ERROR;

	public static String NetMgmtMsg_INTRO;
	public static String NetMgmtMsg_RESUME;
	public static String NetMgmtMsg_FAILED;

	public static String SshNetMgrMsg_WAIT_FOR_ENABLEMENT;
	public static String SshNetMgrMsg_ENABLEMENT_DONE;
	public static String SshNetMgrMsg_DISABLEMENT_DONE;
	public static String SshNetMgrEx_ENABLEMENT_TIMEOUT;

	public static String NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED;

	public static String NetMgmtEx_NO_MGMT_NODE;
	public static String NetMgmtEx_MGMT_NETWORK_NODE_SELECTOR_NOT_MATCH_NODE;
	public static String NetMgmtEx_MISSING_ATTR;
	public static String NetMgmtEx_INVALID_ATTR;
	public static String NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_SELECTOR;
	public static String NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE;
	public static String NetMgmtEx_NO_MGMT_NETWORK_DEVICE;
	public static String NetMgmtEx_MGMT_NETWORK_DEVICE_SELECTOR_NOT_MATCH_NODE;
	public static String NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

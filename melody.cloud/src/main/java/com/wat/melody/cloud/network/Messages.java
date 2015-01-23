package com.wat.melody.cloud.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.network.messages";

	public static String NetworkActivationTimeoutEx_INVALID;

	public static String NetworkActivationProtocolEx_EMPTY;
	public static String NetworkActivationProtocolEx_INVALID;

	public static String NetworkActivationEx_INVALID_XPATH;
	public static String NetworkActivationEx_TOO_MANY_MATCH;
	public static String NetworkActivationEx_NO_MATCH;
	public static String NetworkActivationEx_NOT_MATCH_ELMT;
	public static String NetworkActivationEx_INVALID_NETWORK_ACTIVATION_HOST;

	public static String NetworkActivatorMsg_INTRO;
	public static String NetworkActivatorMsg_RESUME;
	public static String NetworkActivatorEx_CREATION_FAILED;

	public static String WinRmNetworkActivatorEx_NOT_SUPPORTED;

	public static String SshNetworkActivatorMsg_WAIT_FOR_ENABLEMENT;
	public static String SshNetworkActivatorMsg_ENABLEMENT_DONE;
	public static String SshNetworkActivatorMsg_DISABLEMENT_DONE;
	public static String SshNetworkActivatorEx_ENABLEMENT_TIMEOUT;

	public static String TelnetNetworkActivatorMsg_WAIT_FOR_ENABLEMENT;
	public static String TelnetNetworkActivatorEx_ENABLEMENT_TIMEOUT;

	public static String NetworkDeviceListEx_DEVICE_ALREADY_DEFINE;

	public static String NetworkDevLoaderEx_MISSING_ATTR;
	public static String NetworkDevLoaderEx_GENERIC_ERROR;
	public static String NetworkDevLoaderEx_EMPTY_NETDEV_LIST;

	public static String NetMgmtEx_MISSING;
	public static String NetMgmtEx_MISSING_ATTR;
	public static String NetMgmtEx_INVALID_ATTR;
	public static String NetMgmtEx_SELECTOR_INVALID_XPATH;
	public static String NetMgmtEx_SELECTOR_NOT_MATCH_ELMT;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
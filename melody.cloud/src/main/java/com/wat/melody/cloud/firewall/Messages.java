package com.wat.melody.cloud.firewall;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.firewall.messages";

	public static String TcpMgmtEx_SELECTOR_INVALID_XPATH;
	public static String TcpMgmtEx_SELECTOR_NOT_MATCH_ELMT;
	public static String UdpMgmtEx_SELECTOR_INVALID_XPATH;
	public static String UdpMgmtEx_SELECTOR_NOT_MATCH_ELMT;
	public static String IcmpMgmtEx_SELECTOR_INVALID_XPATH;
	public static String IcmpMgmtEx_SELECTOR_NOT_MATCH_ELMT;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
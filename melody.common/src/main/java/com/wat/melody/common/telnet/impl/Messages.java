package com.wat.melody.common.telnet.impl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.telnet.impl.messages";

	public static String SessionEx_CNX_INTERRUPTED;
	public static String SessionEx_FAILED_TO_CONNECT;
	public static String SessionMsg_RETRY_TO_CONNECT;
	public static String SessionMsg_CNX;
	public static String SessionMsg_CNX_OK;

	public static String ExecEx_INTERRUPTED;
	public static String ExecMsg_GRACEFULL_STOP;
	public static String ExecMsg_FORCE_STOP;
	public static String ExecMsg_FORCE_STOP_DONE;
	public static String ExecMsg_FORCE_STOP_AVOID;
	public static String ExecMsg_MULTILINE_REFUSED;
	public static String ExecMsg_ECHO_REFUSED;
	public static String ExecMsg_PROMPT_REFUSED;
	public static String ExecMsg_CRLF_REFUSED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
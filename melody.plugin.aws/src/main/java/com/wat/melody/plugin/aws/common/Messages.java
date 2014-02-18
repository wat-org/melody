package com.wat.melody.plugin.aws.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.aws.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_READ_TIMEOUT;
	public static String ConfEx_INVALID_CONNECTION_TIMEOUT;
	public static String ConfEx_INVALID_RETRY;
	public static String ConfEx_INVALID_MAX_CONN;
	public static String ConfEx_INVALID_SEND_BUFFSIZE;
	public static String ConfEx_INVALID_RECEIVE_BUFFSIZE;
	public static String ConfEx_INVALID_PROTOCOL;
	public static String ConfEx_INVALID_PROXY_HOST;
	public static String ConfEx_INVALID_PROXY_PORT;
	public static String ConfEx_PROVIDER_CNF;
	public static String ConfEx_PROVIDER_NCDF;
	public static String ConfEx_PROVIDER_IS;
	public static String ConfEx_PROVIDER_IE;
	public static String ConfEx_INVALID_AWS_CREDENTIALS;
	public static String ConfEx_VALIDATION;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
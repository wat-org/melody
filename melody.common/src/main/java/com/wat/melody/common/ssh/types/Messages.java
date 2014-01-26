package com.wat.melody.common.ssh.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.ssh.types.messages";

	public static String ConnectionRetryEx_EMPTY;
	public static String ConnectionRetryEx_NOT_A_NUMBER;
	public static String ConnectionRetryEx_NEGATIVE;

	public static String CompressionLevelEx_EMPTY;
	public static String CompressionLevelEx_INVALID;

	public static String CompressionTypeEx_EMPTY;
	public static String CompressionTypeEx_INVALID;

	public static String CnxTimeoutEx_INVALID;

	public static String ProxyTypeEx_EMPTY;
	public static String ProxyTypeEx_INVALID;

	public static String ReadTimeoutEx_INVALID;

	public static String ServerAliveIntervalEx_INVALID;

	public static String ServerAliveMaxCountEx_EMPTY;
	public static String ServerAliveMaxCountEx_NOT_A_NUMBER;
	public static String ServerAliveMaxCountEx_NEGATIVE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
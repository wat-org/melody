package com.wat.melody.common.telnet.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.telnet.types.messages";

	public static String CnxTimeoutEx_INVALID;

	public static String ConnectionRetryEx_EMPTY;
	public static String ConnectionRetryEx_NOT_A_NUMBER;
	public static String ConnectionRetryEx_NEGATIVE;

	public static String ReadTimeoutEx_INVALID;

	public static String ReceiveBufferSizeEx_EMPTY;
	public static String ReceiveBufferSizeEx_NOT_A_NUMBER;
	public static String ReceiveBufferSizeEx_NEGATIVE;

	public static String SoLingerEx_EMPTY;
	public static String SoLingerEx_NOT_A_NUMBER;
	public static String SoLingerEx_NEGATIVE;

	public static String SendBufferSizeEx_EMPTY;
	public static String SendBufferSizeEx_NOT_A_NUMBER;
	public static String SendBufferSizeEx_NEGATIVE;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
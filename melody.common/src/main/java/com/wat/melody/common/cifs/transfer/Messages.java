package com.wat.melody.common.cifs.transfer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.cifs.transfer.messages";

	public static String CifsEx_MKDIR;
	public static String CifsEx_RMDIR;
	public static String CifsEx_RM;
	public static String CifsEx_STAT;
	public static String CifsEx_LS;
	public static String CifsEx_CHA;
	public static String CifsEx_CHH;
	public static String CifsEx_CHR;
	public static String CifsEx_CHS;
	public static String CifsEx_PUT;
	public static String CifsEx_GET;

	public static String CifsFSEx_FAILED_TO_SET_ATTRIBUTES;
	public static String CifsFSEx_FAILED_TO_SET_ATTRIBUTE;
	public static String CifsFSEx_FAILED_TO_SET_ATTRIBUTE_X;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
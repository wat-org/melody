package com.wat.melody.plugin.aws.s3.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.aws.s3.common.messages";

	public static String S3Ex_INVALID_REGION;

	public static String CreateBucketMsg_ALREADY_EXISTS;
	public static String CreateBucketEx_GENERIC_FAIL;

	public static String DeleteBucketMsg_NOT_EXISTS;
	public static String DeleteBucketEx_GENERIC_FAIL;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
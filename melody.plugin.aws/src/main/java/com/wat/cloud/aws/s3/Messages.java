package com.wat.cloud.aws.s3;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.aws.s3.messages";

	public static String BucketNameEx_EMPTY;
	public static String BucketNameEx_INVALID_LENGTH;
	public static String BucketNameEx_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
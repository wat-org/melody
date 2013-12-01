package com.wat.cloud.aws.s3;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.aws.s3.messages";

	public static String DeleteKeyMsg_DELETING;
	public static String DeleteKeyMsg_DELETING_VERSION;
	public static String DeleteKeyMsg_DELETED;
	public static String DeleteKeyMsg_DELETED_VERSION;
	public static String DeleteKeyEx_FAILED;
	public static String DeleteKeyEx_FAILED_VERSION;
	public static String DeleteKeyEx_ERROR_SUMMARY;
	public static String DeleteKeyEx_INTERRUPTED;

	public static String BucketNameEx_EMPTY;
	public static String BucketNameEx_INVALID_LENGTH;
	public static String BucketNameEx_INVALID;

	public static String S3FSEx_STAT;
	public static String S3FSEx_RM;
	public static String S3FSEx_LN;
	public static String S3FSEx_MKDIR;
	public static String S3FSEx_RMDIR_INTERRUPTED;
	public static String S3FSEx_RMDIR;
	public static String S3FSEx_LS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
package com.wat.cloud.aws.s3;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.aws.s3.messages";

	public static String BucketNameEx_EMPTY;
	public static String BucketNameEx_INVALID_LENGTH;
	public static String BucketNameEx_INVALID;

	public static String DeleteKeyMsg_DELETING;
	public static String DeleteKeyMsg_DELETING_VERSION;
	public static String DeleteKeyMsg_DELETED;
	public static String DeleteKeyMsg_DELETED_VERSION;
	public static String DeleteKeyEx_FAILED;
	public static String DeleteKeyEx_FAILED_VERSION;
	public static String DeleteKeyEx_ERROR_SUMMARY;
	public static String DeleteKeyEx_INTERRUPTED;

	public static String StorageModeEx_EMPTY;
	public static String StorageModeEx_INVALID;

	public static String S3fsEx_STAT;
	public static String S3fsEx_LN;
	public static String S3fsEx_MKDIR;
	public static String S3fsEx_PUT;
	public static String S3fsEx_GET;
	public static String S3fsEx_RMDIR;
	public static String S3fsEx_RMDIR_INTERRUPTED;
	public static String S3fsEx_RM;
	public static String S3fsEx_LS;
	public static String S3fsEx_SETATTRS;

	public static String S3fsMsg_GET_ENCRYPTED;
	public static String S3fsEx_PUT_INTERRUPTED;
	public static String S3fsEx_GET_INTERRUPTED;

	public static String S3fsEx_FAILED_TO_SET_ATTRIBUTES;
	public static String S3fsEx_FAILED_TO_SET_ATTRIBUTE;
	public static String S3fsEx_FAILED_TO_SET_ATTRIBUTE_X;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
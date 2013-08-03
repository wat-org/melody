package com.wat.melody.common.files;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.files.messages";

	public static String DirEx_NOT_A_DIR;
	public static String DirEx_CANT_READ;
	public static String DirEx_CANT_WRITE;
	public static String DirEx_NOT_FOUND;
	public static String DirEx_INVALID_PARENT;

	public static String FileEx_NOT_A_FILE;
	public static String FileEx_CANT_READ;
	public static String FileEx_CANT_WRITE;
	public static String FileEx_NOT_FOUND;

	public static String LocalFSEx_FAILED_TO_SET_ATTRIBUTES;
	public static String LocalFSEx_FAILED_TO_SET_ATTRIBUTE;
	public static String LocalFSEx_FAILED_TO_SET_ATTRIBUTE_X;

	public static String PoxixGroupEx_EMPTY;
	public static String PoxixGroupEx_INVALID;

	public static String PosixPermissionsEx_EMPTY;
	public static String PosixPermissionsEx_INVALID;

	public static String PoxixUserEx_EMPTY;
	public static String PoxixUserEx_INVALID;

	public static String TarGzEx_NOT_A_TARGZ;
	public static String TarGzEx_INVALID_EXTENSION;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
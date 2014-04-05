package com.wat.melody.cloud.protectedarea;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.protectedarea.messages";

	public static String CreateMsg_EXISTS;

	public static String DestroyMsg_ID_NOT_DEFINED;
	public static String DestroyMsg_NOT_EXISTS;

	public static String UpdateContentMsg_ID_NOT_DEFINED;
	public static String UpdateContentEx_ID_INVALID;
	public static String UpdateContentMsg_FWRULES_RESUME;

	public static String ProtectedAreaIdEx_EMPTY;
	public static String ProtectedAreaIdEx_INVALID;

	public static String ProtectedAreaIdsEx_EMPTY_NAME;
	public static String ProtectedAreaIdsEx_INVALID_NAME;

	public static String ProtectedAreaNameEx_EMPTY;
	public static String ProtectedAreaNameEx_INVALID;

	public static String ProtectedAreaNamesEx_EMPTY_NAME;
	public static String ProtectedAreaNamesEx_INVALID_NAME;

	public static String ProtectedAreaEx_SELECTOR_NOT_XPATH;
	public static String ProtectedAreaEx_NOT_DEFINED;
	public static String ProtectedAreaEx_MULTIPLE_DEFINITION;
	public static String ProtectedAreaEx_ID_NOT_DEFINED;
	public static String ProtectedAreaEx_ID_EMPTY;
	public static String ProtectedAreaEx_ID_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
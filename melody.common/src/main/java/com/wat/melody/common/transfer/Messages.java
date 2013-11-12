package com.wat.melody.common.transfer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.transfer.messages";

	public static String LinkOptionEx_EMPTY;
	public static String LinkOptionEx_INVALID;

	public static String LocalFSEx_COPY;
	public static String LocalFSEx_COPY_INTERRUPTED;

	public static String ScopeEx_EMPTY;
	public static String ScopeEx_INVALID;

	public static String ScopesEx_EMPTY_SCOPE;
	public static String ScopesEx_INVALID_SCOPE;
	public static String ScopesEx_EMPTY;

	public static String TransferBehaviorEx_EMPTY;
	public static String TransferBehaviorEx_INVALID;

	public static String TransferEx_LISTING_UNMANAGED;
	public static String TransferEx_LISTING_MANAGED;
	public static String TransferEx_LISTING_INTERRUPTED;
	public static String TransferEx_UNMANAGED;
	public static String TransferEx_MANAGED;
	public static String TransferEx_INTERRUPTED;
	public static String TransferEx_FAILED;
	public static String TransferEx_TRANSFER_INTERRUPTED;
	public static String TransferMsg_DISPLAY_RESOURCE_TREE;
	public static String TransferMsg_IGNORE_IOERROR;
	public static String TransferMsg_START;
	public static String TransferMsg_FINISH;
	public static String TransferMsg_BEGIN;
	public static String TransferMsg_END;
	public static String TransferMsg_SKIP_ATTR;
	public static String TransferMsg_SKIP_LINK;
	public static String TransferMsg_LINK_COPY_UNSAFE_IMPOSSIBLE;
	public static String TransferMsg_LINK_SKIPPED;
	public static String TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS;
	public static String TransferMsg_DONT_TRANSFER_CAUSE_LINK_ALREADY_EXISTS;
	public static String TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
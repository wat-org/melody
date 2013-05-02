package com.wat.melody.common.keypair;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.keypair.messages";

	public static String KeyPairNameEx_EMPTY;
	public static String KeyPairNameEx_INVALID;

	public static String KeyPairRepoPathEx_INVALID_REPO_PATH;
	public static String KeyPairRepoPathEx_FAILED_TO_CREATE_REPO;

	public static String KeyPairRepoMsg_GENKEY_BEGIN;
	public static String KeyPairRepoMsg_GENKEY_END;
	public static String KeyPairRepoMsg_DELKEY_BEGIN;
	public static String KeyPairRepoMsg_DELKEY_END;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

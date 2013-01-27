package com.wat.melody.common.keypair;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.keypair.messages";

	public static String KeyPairNameEx_EMPTY;
	public static String KeyPairNameEx_INVALID;

	public static String KeyPairRepoEx_INVALID_REPO_PATH;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

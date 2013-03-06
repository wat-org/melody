package com.wat.melody.cloud.firewall;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.firewall.messages";

	public static String FWRulesMgmtEx_INVALID_FWRULES_SELECTOR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

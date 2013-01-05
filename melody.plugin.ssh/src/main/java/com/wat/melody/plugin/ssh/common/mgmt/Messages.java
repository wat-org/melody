package com.wat.melody.plugin.ssh.common.mgmt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.ssh.common.mgmt.messages";

	public static String SshMgmtCnxMsg_OPENING;
	public static String SshMgmtCnxMsg_OPENED;
	public static String SshMgmtCnxMsg_DEPLOYING;
	public static String SshMgmtCnxMsg_DEPLOYED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

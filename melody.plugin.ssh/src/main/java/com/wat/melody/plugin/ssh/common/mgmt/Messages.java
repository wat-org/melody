package com.wat.melody.plugin.ssh.common.mgmt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.ssh.common.mgmt.messages";

	public static String SshMgmtCnxMsg_OPENING;
	public static String SshMgmtCnxMsg_OPENED;
	public static String SshMgmtCnxMsg_DEPLOYING;
	public static String SshMgmtCnxMsg_DEPLOYED;

	public static String SshMgmtCnxEx_GENERIC_FAIL;
	public static String SshMgmtCnxEx_NO_KEY;
	public static String SshMgmtCnxEx_INVALID_KEY;
	public static String SshMgmtCnxEx_INVALID_MASTER_KEY;
	public static String SshMgmtCnxEx_INVALID_MASTER_CREDENTIALS;

	public static String SshMgmtCnxEx_USERADD_FAIL;
	public static String SshMgmtCnxEx_UMASK_FAIL;
	public static String SshMgmtCnxEx_MKDIR_FAIL;
	public static String SshMgmtCnxEx_CHOWN_SSH_FAIL;
	public static String SshMgmtCnxEx_TOUCH_AUTH_FAIL;
	public static String SshMgmtCnxEx_CHOWN_AUTH_FAIL;
	public static String SshMgmtCnxEx_ADD_KEY_FAIL;
	public static String SshMgmtCnxEx_SELIUNX_FAIL;
	public static String SshMgmtCnxEx_DEPLOY_GENERIC_FAIL;
	public static String SshMgmtCnxEx_DEPLOY_INTERRUPTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

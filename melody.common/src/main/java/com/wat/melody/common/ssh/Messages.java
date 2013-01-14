package com.wat.melody.common.ssh;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.ssh.messages";

	/*
	 * TODO : error message
	 */
	public static String CompressionLevelEx_EMPTY;
	public static String CompressionLevelEx_INVALID;

	public static String CompressionTypeEx_EMPTY;
	public static String CompressionTypeEx_INVALID;

	public static String ProxyTypeEx_EMPTY;
	public static String ProxyTypeEx_INVALID;

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_KNOWNHOSTS;
	public static String ConfEx_INVALID_KEYPAIR_SIZE;
	public static String ConfEx_INVALID_CONNECTION_TIMEOUT;
	public static String ConfEx_INVALID_READ_TIMEOUT;
	public static String ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT;
	public static String ConfEx_INVALID_SERVER_ALIVE_INTERVAL;

	public static String SessionEx_FAILED_TO_CONNECT;
	public static String SessionExecEx_EXEC_INTERRUPTED;
	public static String SessionExecMsg_GRACEFULL_SHUTDOWN;
	public static String SessionExecMsg_FORCE_SHUTDOWN;

	public static String SshMgmtCnxMsg_OPENING;
	public static String SshMgmtCnxMsg_OPENED;
	public static String SshMgmtCnxMsg_DEPLOYING;
	public static String SshMgmtCnxMsg_DEPLOYED;
	public static String SshMgmtCnxEx_GENERIC_FAIL;
	public static String SshMgmtCnxEx_NO_KEY;
	public static String SshMgmtCnxEx_INVALID_KEY;
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

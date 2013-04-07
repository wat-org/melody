package com.wat.melody.common.ssh;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.ssh.messages";

	public static String KnownHostsEx_INVALID_CONTENT;

	public static String KnownHostsRepoPathEx_INVALID_REPO_PATH;
	public static String KnownHostsRepoPathEx_FAILED_TO_CREATE_REPO;

	public static String SessionEx_FAILED_TO_CONNECT;
	public static String SessionMsg_CNX;
	public static String SessionMsg_CNX_OK;

	public static String UploadEx_FAILED;
	public static String UploadEx_UNMANAGED;
	public static String UploadEx_MANAGED;
	public static String UploadEx_INTERRUPTED;
	public static String UploadMsg_GRACEFUL_SHUTDOWN;
	public static String UploadMsg_START;
	public static String UploadMsg_BEGIN;
	public static String UploadMsg_END;
	public static String UploadMsg_FINISH;
	public static String UploadMsg_NOTFOUND;
	public static String UploadMsg_COPY_UNSAFE_IMPOSSIBLE;

	public static String UploadEx_STAT;
	public static String UploadEx_LN;
	public static String UploadEx_MKDIR;
	public static String UploadEx_MKDIRS;
	public static String UploadEx_CHMOD;
	public static String UploadEx_CHGRP;
	public static String UploadEx_PUT;

	public static String ExecEx_INTERRUPTED;
	public static String ExecMsg_GRACEFULL_SHUTDOWN;
	public static String ExecMsg_FORCE_SHUTDOWN;

	public static String SshMgmtCnxEx_GENERIC_FAIL;
	public static String SshMgmtCnxEx_INVALID_MASTER_CREDENTIALS;
	public static String SshMgmtCnxEx_DEPLOY_INTERRUPTED;
	public static String SshMgmtCnxEx_USERADD_FAIL;
	public static String SshMgmtCnxEx_UMASK_FAIL;
	public static String SshMgmtCnxEx_MKDIR_FAIL;
	public static String SshMgmtCnxEx_CHOWN_SSH_FAIL;
	public static String SshMgmtCnxEx_TOUCH_AUTH_FAIL;
	public static String SshMgmtCnxEx_CHOWN_AUTH_FAIL;
	public static String SshMgmtCnxEx_ADD_KEY_FAIL;
	public static String SshMgmtCnxEx_SELIUNX_FAIL;
	public static String SshMgmtCnxEx_DEPLOY_FAIL;
	public static String SshMgmtCnxEx_NO_SUDO;
	public static String SshMgmtCnxEx_NO_AUTH_SUDO;
	public static String SshMgmtCnxMsg_CNX_USER_FAIL;
	public static String SshMgmtCnxMsg_OPENED;
	public static String SshMgmtCnxMsg_DEPLOYING;
	public static String SshMgmtCnxMsg_DEPLOYED;
	public static String SshMgmtCnxMsg_CNX_USER_OK;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

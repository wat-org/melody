package com.wat.melody.common.ssh.impl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.ssh.impl.messages";

	public static String KnownHostsEx_INVALID_CONTENT;

	public static String KnownHostsRepoPathEx_INVALID_REPO_PATH;
	public static String KnownHostsRepoPathEx_FAILED_TO_CREATE_REPO;

	public static String SessionEx_FAILED_TO_CONNECT;
	public static String SessionMsg_CNX;
	public static String SessionMsg_CNX_OK;

	public static String SftpEx_LSTAT;
	public static String SftpEx_STAT;
	public static String SftpEx_LN;
	public static String SftpEx_MKDIR;
	public static String SfptEx_PUT;
	public static String SfptEx_GET;
	public static String SftpEx_RMDIR;
	public static String SftpEx_RM;
	public static String SftpEx_READLINK;
	public static String SftpEx_LS;
	public static String SftpEx_CHMOD;
	public static String SftpEx_CHOWN;
	public static String SftpEx_CHGRP;

	public static String SfptEx_PUT_INTERRUPTED;
	public static String SfptEx_GET_INTERRUPTED;

	public static String SftpFSEx_SET_ATTRIBUTES_NOT_SUPPORTED_ON_LINK;
	public static String SftpFSEx_FAILED_TO_SET_ATTRIBUTES;
	public static String SftpFSEx_FAILED_TO_SET_ATTRIBUTE;
	public static String SftpFSEx_FAILED_TO_SET_ATTRIBUTE_X;

	public static String ExecEx_INTERRUPTED;
	public static String ExecMsg_GRACEFULL_STOP;
	public static String ExecMsg_FORCE_STOP;
	public static String ExecMsg_FORCE_STOP_DONE;
	public static String ExecMsg_FORCE_STOP_AVOID;

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
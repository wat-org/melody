package com.wat.melody.plugin.ssh.common;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.ssh.common.messages";

	public static String CompressionLevelEx_EMPTY;
	public static String CompressionLevelEx_INVALID;

	public static String CompressionTypeEx_EMPTY;
	public static String CompressionTypeEx_INVALID;

	public static String KeyPairRepoEx_PRIVATE_KEY_NOT_FOUND;
	public static String KeyPairRepoEx_PUBLIC_KEY_NOT_FOUND;
	public static String KeyPairRepoEx_FINGERPRINT_NOT_FOUND;

	public static String ProxyTypeEx_EMPTY;
	public static String ProxyTypeEx_INVALID;

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_KNOWNHOSTS;
	public static String ConfEx_INVALID_KEYPAIR_REPO;
	public static String ConfEx_INVALID_KEYPAIR_SIZE;
	public static String ConfEx_INVALID_CONNECTION_TIMEOUT;
	public static String ConfEx_INVALID_READ_TIMEOUT;
	public static String ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT;
	public static String ConfEx_INVALID_SERVER_ALIVE_INTERVAL;
	public static String ConfEx_CONF_NOT_REGISTERED;
	public static String ConfEx_CONF_REGISTRATION_ERROR;

	public static String SshEx_EMPTY_LOGIN_ATTR;
	public static String SshEx_EMPTY_KEYPAIR_NAME_ATTR;
	public static String SshEx_INVALID_KEYPAIR_NAME_ATTR;
	public static String SshEx_INVALID_KEYPAIR_REPO_ATTR;
	public static String SshEx_BOTH_PASSWORD_OR_PK_ATTR;
	public static String SshEx_MISSING_PASSWORD_OR_PK_ATTR;
	public static String SshEx_FAILED_TO_CONNECT;

	public static String SshEx_BOTH_COMMAND_OR_SCRIPT_ATTR;
	public static String SshEx_MISSING_COMMAND_OR_SCRIPT_ATTR;
	public static String SshEx_READ_IO_ERROR;
	public static String SshEx_WRITE_IO_ERROR;
	public static String SshMsg_WAIT_FOR_MANAGEMENT;

	public static String UploadEx_INVALID_NE;
	public static String UploadEx_INVALID_MAXPAR_ATTR;
	public static String UploadEx_MISSING_ATTR;
	public static String UploadEx_NOTFOUND;
	public static String UploadEx_FAILED;
	public static String UploadEx_INTERRUPTED;
	public static String UploadEx_MANAGED;
	public static String UploadEx_UNMANAGED;
	public static String UploadMsg_COPY_UNSAFE_IMPOSSIBLE;

	public static String UploadEx_STAT;
	public static String UploadEx_LN;
	public static String UploadEx_MKDIR;
	public static String UploadEx_MKDIRS;
	public static String UploadEx_CD;
	public static String UploadEx_PWD;
	public static String UploadEx_CHMOD;
	public static String UploadEx_CHGRP;
	public static String UploadEx_PUT;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

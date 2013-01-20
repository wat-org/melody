package com.wat.melody.plugin.ssh.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.ssh.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_KNOWNHOSTS;
	public static String ConfEx_INVALID_KEYPAIR_SIZE;

	public static String SshEx_INVALID_KEYPAIR_NAME_ATTR;
	public static String SshEx_MISSING_PASSWORD_OR_PK_ATTR;
	public static String SshEx_BOTH_COMMAND_OR_SCRIPT_ATTR;
	public static String SshEx_MISSING_COMMAND_OR_SCRIPT_ATTR;
	public static String SshEx_READ_IO_ERROR;
	public static String SshEx_WRITE_IO_ERROR;

	public static String UploadEx_INVALID_NE;
	public static String UploadEx_INVALID_MAXPAR_ATTR;
	public static String UploadEx_MISSING_ATTR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

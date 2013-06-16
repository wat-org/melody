package com.wat.melody.cli;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cli.messages";

	public static String CmdEx_MULTIPLE_GLOBAL_CONF_FILE_ERROR;
	public static String CmdEx_MISSING_OPTION_SPECIFIER;
	public static String CmdEx_UNKNOWN_OPTION_SPECIFIER;
	public static String CmdEx_MISSING_OPTION_VALUE;
	public static String CmdEx_INVALID_OPTION_VALUE;
	public static String CmdEx_TOOMUCH_LOG_THRESHOLD;
	public static String CmdEx_UNKNOWN_ARGUMENT_ERROR;
	public static String CmdEx_MISSING_SD;
	public static String CmdEx_GENERIC_PARSE;

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_INTEGER_FORMAT;
	public static String ConfEx_GENERIC_GLOBAL_CONF_LOAD;

	public static String ConfEx_MISSING_TASKS_DIRECTIVE;
	public static String ConfEx_EMPTY_TASKS_DIRECTIVE;
	public static String ConfEx_CNF_TASKS_DIRECTIVE;
	public static String ConfEx_NCDF_TASKS_DIRECTIVE;
	public static String ConfEx_IS_TASKS_DIRECTIVE;
	public static String ConfEx_IE_TASKS_DIRECTIVE;

	public static String ConfEx_MISSING_PLUGINS_DIRECTIVE;
	public static String ConfEx_EMPTY_PLUGINS_DIRECTIVE;
	public static String ConfEx_NVF_PLUGINS_DIRECTIVE;
	public static String ConfEx_NVPS_PLUGINS_DIRECTIVE;

	public static String ConfEx_MISSING_PCC_DIRECTIVE;
	public static String ConfEx_EMPTY_PCC_DIRECTIVE;
	public static String ConfEx_CNF_CONF_DIRECTIVE;
	public static String ConfEx_NCDF_CONF_DIRECTIVE;
	public static String ConfEx_IS_CONF_DIRECTIVE;
	public static String ConfEx_IE_CONF_DIRECTIVE;
	public static String ConfEx_DUPLICATE_CONF_DIRECTIVE;
	public static String ConfEx_GENERIC_PLUGIN_LOAD;

	public static String ShutdownHookMsg_EXITING;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
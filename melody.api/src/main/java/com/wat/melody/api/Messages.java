package com.wat.melody.api;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.api.messages";

	public static String ConfEx_CONF_NOT_REGISTERED;

	public static String TaskFactoryEx_INVALID_ATTR;
	public static String TaskFactoryEx_MANDATORY_ATTR_NOT_FOUND;
	public static String TaskFactoryEx_ATTR_SPEC_CONFLICT;
	public static String TaskFactoryEx_INVALID_NE;
	public static String TaskFactoryEx_MANDATORY_NE_NOT_FOUND;
	public static String TaskFactoryEx_NO_CONSTRUCTOR_MATCH;
	public static String TaskFactoryEx_ADD_NE_SPEC_CONFLICT;
	public static String TaskFactoryEx_CREATE_NE_SPEC_CONFLICT;
	public static String TaskFactoryEx_TOPLEVEL_ERROR;
	public static String TaskFactoryEx_FIRSTLEVEL_ERROR;
	public static String TaskFactoryEx_CHILD_ERROR;
	public static String TaskFactoryEx_UNDEF_TASK;
	public static String TaskFactoryEx_CONVERT_ATTR;
	public static String TaskFactoryEx_CONVERT_ATTR_TO_ENUM;
	public static String TaskFactoryEx_EXPAND_ATTR;
	public static String TaskFactoryEx_CREATE_ATTR;
	public static String TaskFactoryEx_SET_ATTR;
	public static String TaskFactoryEx_SET_NE;

	public static String TaskEx_INIT_FINAL_STATE;
	public static String TaskEx_PROCESS_FINAL_STATE;

	public static String ProcMgrEx_HARD_KILL_TIMEOUT;
	public static String ProcMgrEx_MAX_PAR;
	public static String ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE;
	public static String ProcMgrEx_PROCESS_FINAL_STATE;

	public static String OrderEx_DUPLICATE;
	public static String OrderEx_UNDEF;

	public static String PMFactoryEx_UNDEF_ENV;
	public static String PMFactoryEx_CLASS_NOT_FOUND;
	public static String PMFactoryEx_NO_CLASS_DEF_FOUND;
	public static String PMFactoryEx_ILLEGAL_ACCESS;
	public static String PMFactoryEx_CLASS_CAST;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

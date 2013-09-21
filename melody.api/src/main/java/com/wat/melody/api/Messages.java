package com.wat.melody.api;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.api.messages";

	public static String ConfEx_CONF_NOT_REGISTERED;

	public static String TaskRegistrationEx_DUPLICATE;

	public static String TaskFactoryEx_MUST_BE_TOPLEVEL;
	public static String TaskFactoryEx_CANNOT_BE_TOPLEVEL;
	public static String TaskFactoryEx_MUST_BE_FIRSTLEVEL;
	public static String TaskFactoryEx_CANNOT_BE_FIRSTLEVEL;
	public static String TaskFactoryEx_UNDEF_TASK;
	public static String TaskFactoryEx_INVALID_ATTR;
	public static String TaskFactoryEx_MANDATORY_ATTR_NOT_FOUND;
	public static String TaskFactoryEx_ATTR_DUPLICATE;
	public static String TaskFactoryEx_ATTR_SPEC_CONFLICT;
	public static String TaskFactoryEx_INVALID_TEXT;
	public static String TaskFactoryEx_MANDATORY_TEXT_NOT_FOUND;
	public static String TaskFactoryEx_TEXT_DUPLICATE;
	public static String TaskFactoryEx_TEXT_SPEC_CONFLICT;
	public static String TaskFactoryEx_INVALID_NE;
	public static String TaskFactoryEx_MANDATORY_NE_NOT_FOUND;
	public static String TaskFactoryEx_NO_CONSTRUCTOR_MATCH;
	public static String TaskFactoryEx_NE_DUPLICATE;
	public static String TaskFactoryEx_ADD_NE_SPEC_CONFLICT;
	public static String TaskFactoryEx_CREATE_NE_SPEC_CONFLICT;
	public static String TaskFactoryEx_CREATE_NE_SPEC_RT_CONFLICT;
	public static String TaskFactoryEx_CONVERT_ATTR;
	public static String TaskFactoryEx_CONVERT_ATTR_TO_ENUM;
	public static String TaskFactoryEx_EXPAND_ATTR;
	public static String TaskFactoryEx_CREATE_ATTR;
	public static String TaskFactoryEx_SET_ATTR;
	public static String TaskFactoryEx_EXPAND_TEXT;
	public static String TaskFactoryEx_CREATE_TEXT;
	public static String TaskFactoryEx_SET_TEXT;
	public static String TaskFactoryEx_SET_NE;

	public static String TaskEx_INIT_FINAL_STATE;
	public static String TaskEx_PROCESS_FINAL_STATE;

	public static String TargetEx_NOT_MATCH_ELEMENT;

	public static String ProcMgrEx_MAX_PAR;
	public static String ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE;
	public static String ProcMgrEx_PROCESS_FINAL_STATE;

	public static String OrderEx_DUPLICATE;
	public static String OrderEx_UNDEF;

	public static String PMFactoryEx_UNDEF_ENV;
	public static String PMFactoryEx_CLASS_NOT_FOUND;
	public static String PMFactoryEx_NO_CLASS_DEF_FOUND;
	public static String PMFactoryEx_INVALID_SPEC;
	public static String PMFactoryEx_INTERNAL_ERROR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
package com.wat.melody.plugin.aws.ec2.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.aws.ec2.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_READ_TIMEOUT;
	public static String ConfEx_INVALID_CONNECTION_TIMEOUT;
	public static String ConfEx_INVALID_RETRY;
	public static String ConfEx_INVALID_MAX_CONN;
	public static String ConfEx_INVALID_SEND_BUFFSIZE;
	public static String ConfEx_INVALID_RECEIVE_BUFFSIZE;
	public static String ConfEx_INVALID_PROTOCOL;
	public static String ConfEx_INVALID_PROXY_HOST;
	public static String ConfEx_INVALID_PROXY_PORT;
	public static String ConfEx_CONF_NOT_REGISTERED;
	public static String ConfEx_CONF_REGISTRATION_ERROR;
	public static String ConfEx_INVALID_AWS_CREDENTIALS;
	public static String ConfEx_VALIDATION;

	public static String MachineEx_MISSING_REGION_ATTR;
	public static String MachineEx_REGION_ERROR;
	public static String MachineEx_INVALID_REGION_ATTR;
	public static String MachineEx_INVALID_TIMEOUT_ATTR;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_XPATH;
	public static String MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH;
	public static String MachineEx_ENABLE_MANAGEMENT_ERROR;
	public static String MachineEx_DISABLE_MANAGEMENT_ERROR;

	public static String NewEx_MISSING_IMAGEID_ATTR;
	public static String NewEx_IMAGEID_ERROR;
	public static String NewEx_INVALID_IMAGEID_ATTR;
	public static String NewEx_MISSING_INSTANCETYPE_ATTR;
	public static String NewEx_INSTANCETYPE_ERROR;
	public static String NewEx_AVAILABILITYZONE_ERROR;
	public static String NewEx_INVALID_AVAILABILITYZONE_ATTR;
	public static String NewEx_MISSING_KEYPAIR_NAME_ATTR;
	public static String NewEx_KEYPAIR_NAME_ERROR;

	public static String CreateEx_GENERIC_FAIL;

	public static String DestroyEx_GENERIC_FAIL;

	public static String StartEx_GENERIC_FAIL;

	public static String StopEx_GENERIC_FAIL;

	public static String ResizeEx_MISSING_INSTANCETYPE_ATTR;
	public static String ResizeEx_INSTANCETYPE_ERROR;
	public static String ResizeEx_INVALID_INSTANCETYPE_ATTR;
	public static String ResizeEx_GENERIC_FAIL;

	public static String UpdateDiskDevEx_GENERIC_FAIL;

	public static String UpdateNetDevEx_GENERIC_FAIL;

	public static String UpdateFireWallEx_GENERIC_FAIL;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String bind(String message, Object... bindings) {
		return NLS.bind(message, bindings);
	}

	private Messages() {
	}

}

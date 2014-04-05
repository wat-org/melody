package com.wat.melody.plugin.libvirt.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.libvirt.common.messages";

	public static String ConfEx_MISSING_DIRECTIVE;
	public static String ConfEx_INVALID_DIRECTIVE;
	public static String ConfEx_EMPTY_DIRECTIVE;
	public static String ConfEx_INVALID_ENDPOINT_CONTEXT_ROOT;

	public static String MachineEx_INVALID_TARGET_ATTR_NOT_XPATH;
	public static String MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH;
	public static String MachineEx_MISSING_REGION_ATTR;
	public static String MachineEx_INVALID_REGION_ATTR;
	public static String MachineEx_MISSING_INSTANCETYPE_ATTR;
	public static String MachineEx_MISSING_IMAGEID_ATTR;
	public static String MachineEx_INVALID_IMAGEID_ATTR;
	public static String MachineEx_MISSING_KEYPAIR_NAME_ATTR;
	public static String MachineEx_MISSING_NAME_ATTR;

	public static String CreateEx_MISSING_PASSPHRASE_ATTR;
	public static String CreateEx_INVALID_PASSPHRASE_ATTR;
	public static String CreateEx_GENERIC_FAIL;

	public static String DestroyEx_GENERIC_FAIL;

	public static String StartEx_GENERIC_FAIL;

	public static String StopEx_GENERIC_FAIL;

	public static String ResizeEx_GENERIC_FAIL;

	public static String UpdateDiskDevEx_GENERIC_FAIL;

	public static String UpdateNetDevEx_GENERIC_FAIL;

	public static String UpdateFireWallEx_GENERIC_FAIL;

	public static String PACreateEx_GENERIC_FAIL;
	public static String PADestroyEx_GENERIC_FAIL;
	public static String PAContentEx_GENERIC_FAIL;
	public static String PAResetEx_GENERIC_FAIL;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
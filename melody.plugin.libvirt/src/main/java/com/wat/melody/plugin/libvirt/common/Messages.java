package com.wat.melody.plugin.libvirt.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.libvirt.common.messages";

	public static String ConfEx_CONF_NOT_REGISTERED;
	public static String ConfEx_CONF_REGISTRATION_ERROR;

	public static String MachineEx_MISSING_REGION_ATTR;
	public static String MachineEx_REGION_ERROR;
	public static String MachineEx_INVALID_TIMEOUT_ATTR;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_XPATH;
	public static String MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH;

	public static String MachineEx_MANAGEMENT_ENABLE_FAILED;
	public static String MachineEx_MANAGEMENT_DISABLE_FAILED;
	public static String MachineMsg_MANAGEMENT_ENABLE_BEGIN;
	public static String MachineMsg_MANAGEMENT_ENABLE_SUCCESS;
	public static String MachineMsg_MANAGEMENT_DISABLE_BEGIN;
	public static String MachineMsg_MANAGEMENT_DISABLE_SUCCESS;

	public static String NewEx_MISSING_IMAGEID_ATTR;
	public static String NewEx_IMAGEID_ERROR;
	public static String NewEx_INVALID_IMAGEID_ATTR;
	public static String NewEx_MISSING_INSTANCETYPE_ATTR;
	public static String NewEx_INSTANCETYPE_ERROR;
	public static String NewEx_INVALID_INSTANCETYPE_ATTR;
	public static String NewEx_MISSING_KEYPAIR_NAME_ATTR;
	public static String NewEx_KEYPAIR_NAME_ERROR;
	public static String NewEx_EMPTY_KEYPAIR_NAME_ATTR;
	public static String NewEx_INVALID_KEYPAIR_NAME_ATTR;
	public static String NewEx_INVALID_KEYPAIR_REPO_ATTR;
	public static String NewEx_FAILED;
	public static String NewMsg_LIVES;
	public static String NewMsg_GENERIC_WARN;

	public static String DeleteMsg_NO_INSTANCE;
	public static String DeleteMsg_TERMINATED;
	public static String DeleteMsg_GENERIC_WARN;

	public static String UpdateDiskEx_INVALID_DISK_XPATH;
	public static String UpdateDiskMsg_NO_INSTANCE;
	public static String UpdateDiskMsg_DISKS_RESUME;
	public static String UpdateDiskMsg_GENERIC_WARN;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

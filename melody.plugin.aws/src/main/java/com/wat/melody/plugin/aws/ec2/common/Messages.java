package com.wat.melody.plugin.aws.ec2.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.aws.ec2.common.messages";

	public static String InstanceTypeEx_EMPTY;
	public static String InstanceTypeEx_INVALID;

	public static String InstanceStateEx_EMPTY;
	public static String InstanceStateEx_INVALID;

	public static String VolumeStateEx_EMPTY;
	public static String VolumeStateEx_INVALID;

	public static String VolumeAttachmentStateEx_EMPTY;
	public static String VolumeAttachmentStateEx_INVALID;

	public static String ManagementMethodEx_EMPTY;
	public static String ManagementMethodEx_INVALID;

	public static String FwRuleLoadEx_EXPAND_ATTR;
	public static String FwRuleLoadEx_INVALID_ATTR;
	public static String FwRuleLoadEx_MANAGED;

	public static String DiskEx_EMPTY_SIZE_ATTR;
	public static String DiskEx_INVALID_SIZE_ATTR;
	public static String DiskEx_EMPTY_DEVICE_ATTR;
	public static String DiskEx_INVALID_DEVICE_ATTR;

	public static String DiskListEx_DEVICE_ALREADY_DEFINE;
	public static String DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE;

	public static String DiskLoadEx_MISSING_ATTR;
	public static String DiskLoadEx_INVALID_ATTR;
	public static String DiskLoadEx_MANAGED;

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

	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE;
	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE_SUCCEED;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE_SUCCEED;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_SUCCEED;

	public static String MachineEx_MISSING_REGION_ATTR;
	public static String MachineEx_INVALID_REGION_ATTR;
	public static String MachineEx_INVALID_TIMEOUT_ATTR;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_XPATH;
	public static String MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH;
	public static String MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH;
	public static String MachineEx_TOO_MANY_TAG_MGNT;
	public static String MachineEx_NO_TAG_MGNT;
	public static String MachineEx_INVALID_TAG_MGNT;
	public static String MachineEx_INVLIAD_TAG_MGNT_WINRN_SUPPORT;
	public static String MachineEx_TOO_MANY_TAG_MGNT_PORT;
	public static String MachineEx_NO_TAG_MGNT_PORT;
	public static String MachineEx_INVALID_TAG_MGNT_PORT;
	public static String MachineEx_ENABLE_SSH_MGNT_TIMEOUT;
	public static String MachineEx_TIMEOUT;
	public static String MachineMsg_WAIT_FOR_MANAGEMENT;

	public static String NewEx_MISSING_IMAGEID_ATTR;
	public static String NewEx_INVALID_IMAGEID_ATTR;
	public static String NewEx_MISSING_INSTANCETYPE_ATTR;
	public static String NewEx_INVALID_INSTANCETYPE_ATTR;
	public static String NewEx_INVALID_AVAILABILITYZONE_ATTR;
	public static String NewEx_MISSING_KEYPAIR_NAME_ATTR;
	public static String NewEx_EMPTY_KEYPAIR_NAME_ATTR;
	public static String NewEx_INVALID_KEYPAIR_NAME_ATTR;
	public static String NewEx_INVALID_KEYPAIR_REPO_ATTR;
	public static String NewEx_FAILED;
	public static String NewMsg_LIVES;

	public static String StartEx_NO_INSTANCE;
	public static String StartEx_SHUTTING_DOWN;
	public static String StartEx_TERMINATED;
	public static String StartMsg_PENDING;
	public static String StartMsg_RUNNING;
	public static String StartMsg_STOPPING;

	public static String StopEx_NO_INSTANCE;
	public static String StopMsg_ALREADY_STOPPED;

	public static String DeleteMsg_NO_INSTANCE;
	public static String DeleteMsg_TERMINATED;

	public static String IngressEx_INVALID_FWRULE_XPATH;
	public static String IngressMsg_NO_INSTANCE;
	public static String IngressMsg_DENY_NA;
	public static String IngressMsg_FWRULES_RESUME;

	public static String ResizeEx_MISSING_INSTANCETYPE_ATTR;
	public static String ResizeEx_INVALID_INSTANCETYPE_ATTR;
	public static String ResizeEx_NO_INSTANCE;
	public static String ResizeEx_NOT_STOPPED;
	public static String ResizeEx_FAILED;
	public static String ResizeMsg_NO_NEED;

	public static String UpdateDiskEx_UNDEF_ROOT_DEVICE;
	public static String UpdateDiskEx_INCORRECT_ROOT_DEVICE;
	public static String UpdateDiskEx_INVALID_DISK_XPATH;
	public static String UpdateDiskEx_DETACH;
	public static String UpdateDiskEx_CREATE;
	public static String UpdateDiskEx_ATTACH;
	public static String UpdateDiskMsg_NO_INSTANCE;
	public static String UpdateDiskMsg_DISKS_RESUME;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}

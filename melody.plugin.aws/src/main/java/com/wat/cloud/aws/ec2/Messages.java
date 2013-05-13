package com.wat.cloud.aws.ec2;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.cloud.aws.ec2.messages";

	public static String KeyPairEx_DIFFERENT;

	public static String VolumeStateEx_EMPTY;
	public static String VolumeStateEx_INVALID;

	public static String VolumeAttachmentStateEx_EMPTY;
	public static String VolumeAttachmentStateEx_INVALID;

	public static String CommonMsg_AUTHORIZE_FWRULE;
	public static String CommonMsg_REVOKE_FWRULE;
	public static String CommonMsg_SKIP_FWRULE;
	public static String CommonMsg_GENKEY_BEGIN;
	public static String CommonMsg_GENKEY_DUP;
	public static String CommonMsg_GENKEY_END;
	public static String CommonMsg_DELKEY_BEGIN;
	public static String CommonMsg_DELKEY_END;
	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE;
	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_INSTANCE_STATE_SUCCEED;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_VOLUME_STATE_SUCCEED;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_FAILED;
	public static String CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_SUCCEED;
	public static String CommonMsg_ATTACH_NOTWORK_DEVICE_NOT_SUPPORTED;
	public static String CommonMsg_DETACH_NOTWORK_DEVICE_NOT_SUPPORTED;

	public static String CreateEx_TIMEOUT;

	public static String DestroyEx_TIMEOUT;

	public static String StartEx_TIMEOUT;

	public static String StopEx_TIMEOUT;

	public static String ResizeEx_FAILED;

	public static String UpdateDiskDevEx_DETACH;
	public static String UpdateDiskDevEx_CREATE;
	public static String UpdateDiskDevEx_ATTACH;

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

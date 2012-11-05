package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalVolumeAttachmentStateException extends AwsException {

	private static final long serialVersionUID = -112345389964589949L;

	public IllegalVolumeAttachmentStateException() {
		super();
	}

	public IllegalVolumeAttachmentStateException(String msg) {
		super(msg);
	}

	public IllegalVolumeAttachmentStateException(Throwable cause) {
		super(cause);
	}

	public IllegalVolumeAttachmentStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
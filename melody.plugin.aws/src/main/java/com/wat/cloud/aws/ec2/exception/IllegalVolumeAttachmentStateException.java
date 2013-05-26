package com.wat.cloud.aws.ec2.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalVolumeAttachmentStateException extends MelodyException {

	private static final long serialVersionUID = -112345389964589949L;

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
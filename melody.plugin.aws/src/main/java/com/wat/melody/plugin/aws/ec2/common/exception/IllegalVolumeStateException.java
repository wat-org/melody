package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalVolumeStateException extends AwsException {

	private static final long serialVersionUID = -654654322574326576L;

	public IllegalVolumeStateException() {
		super();
	}

	public IllegalVolumeStateException(String msg) {
		super(msg);
	}

	public IllegalVolumeStateException(Throwable cause) {
		super(cause);
	}

	public IllegalVolumeStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
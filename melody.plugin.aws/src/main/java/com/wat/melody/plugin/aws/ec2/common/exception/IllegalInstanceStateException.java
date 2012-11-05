package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalInstanceStateException extends AwsException {

	private static final long serialVersionUID = -768994524679997771L;

	public IllegalInstanceStateException() {
		super();
	}

	public IllegalInstanceStateException(String msg) {
		super(msg);
	}

	public IllegalInstanceStateException(Throwable cause) {
		super(cause);
	}

	public IllegalInstanceStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

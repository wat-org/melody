package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalInstanceTypeException extends AwsException {

	private static final long serialVersionUID = -127998078020202109L;

	public IllegalInstanceTypeException() {
		super();
	}

	public IllegalInstanceTypeException(String msg) {
		super(msg);
	}

	public IllegalInstanceTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalInstanceTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
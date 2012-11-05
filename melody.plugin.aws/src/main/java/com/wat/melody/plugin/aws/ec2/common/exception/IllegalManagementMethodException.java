package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalManagementMethodException extends AwsException {

	private static final long serialVersionUID = -412345389964589949L;

	public IllegalManagementMethodException() {
		super();
	}

	public IllegalManagementMethodException(String msg) {
		super(msg);
	}

	public IllegalManagementMethodException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementMethodException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
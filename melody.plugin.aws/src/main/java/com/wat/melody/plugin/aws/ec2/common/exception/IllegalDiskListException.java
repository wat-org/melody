package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalDiskListException extends AwsException {

	private static final long serialVersionUID = -268994527679997771L;

	public IllegalDiskListException() {
		super();
	}

	public IllegalDiskListException(String msg) {
		super(msg);
	}

	public IllegalDiskListException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskListException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

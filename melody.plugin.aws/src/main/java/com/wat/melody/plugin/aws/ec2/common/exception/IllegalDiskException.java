package com.wat.melody.plugin.aws.ec2.common.exception;

public class IllegalDiskException extends AwsException {

	private static final long serialVersionUID = -268994524679997771L;

	public IllegalDiskException() {
		super();
	}

	public IllegalDiskException(String msg) {
		super(msg);
	}

	public IllegalDiskException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

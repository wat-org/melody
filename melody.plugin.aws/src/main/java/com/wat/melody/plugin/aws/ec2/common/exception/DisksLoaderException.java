package com.wat.melody.plugin.aws.ec2.common.exception;

public class DisksLoaderException extends AwsException {

	private static final long serialVersionUID = 3543652224356775432L;

	public DisksLoaderException() {
		super();
	}

	public DisksLoaderException(String msg) {
		super(msg);
	}

	public DisksLoaderException(Throwable cause) {
		super(cause);
	}

	public DisksLoaderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

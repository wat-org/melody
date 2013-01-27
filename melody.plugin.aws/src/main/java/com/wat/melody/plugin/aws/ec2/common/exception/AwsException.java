package com.wat.melody.plugin.aws.ec2.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsException extends TaskException {

	private static final long serialVersionUID = 7682456925787679884L;

	public AwsException() {
		super();
	}

	public AwsException(String msg) {
		super(msg);
	}

	public AwsException(Throwable cause) {
		super(cause);
	}

	public AwsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.cloud.aws.ec2.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsKeyPairRepositoryException extends MelodyException {

	private static final long serialVersionUID = -324324323564787949L;

	public AwsKeyPairRepositoryException() {
		super();
	}

	public AwsKeyPairRepositoryException(String msg) {
		super(msg);
	}

	public AwsKeyPairRepositoryException(Throwable cause) {
		super(cause);
	}

	public AwsKeyPairRepositoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
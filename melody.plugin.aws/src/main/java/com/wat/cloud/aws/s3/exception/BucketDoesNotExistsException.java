package com.wat.cloud.aws.s3.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class BucketDoesNotExistsException extends MelodyException {

	private static final long serialVersionUID = 6434323424565789884L;

	public BucketDoesNotExistsException(String msg) {
		super(msg);
	}

	public BucketDoesNotExistsException(Throwable cause) {
		super(cause);
	}

	public BucketDoesNotExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
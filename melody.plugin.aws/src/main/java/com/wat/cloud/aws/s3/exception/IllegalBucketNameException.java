package com.wat.cloud.aws.s3.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalBucketNameException extends MelodyException {

	private static final long serialVersionUID = 4878756423421532584L;

	public IllegalBucketNameException(String msg) {
		super(msg);
	}

	public IllegalBucketNameException(Throwable cause) {
		super(cause);
	}

	public IllegalBucketNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
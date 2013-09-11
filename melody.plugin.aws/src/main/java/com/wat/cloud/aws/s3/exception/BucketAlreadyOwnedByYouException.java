package com.wat.cloud.aws.s3.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class BucketAlreadyOwnedByYouException extends MelodyException {

	private static final long serialVersionUID = 2432432453654789884L;

	public BucketAlreadyOwnedByYouException(String msg) {
		super(msg);
	}

	public BucketAlreadyOwnedByYouException(Throwable cause) {
		super(cause);
	}

	public BucketAlreadyOwnedByYouException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.cloud.aws.s3.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DeleteKeyException extends MelodyException {

	private static final long serialVersionUID = 3878756523456472584L;

	public DeleteKeyException(String msg) {
		super(msg);
	}

	public DeleteKeyException(Throwable cause) {
		super(cause);
	}

	public DeleteKeyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
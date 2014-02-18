package com.wat.cloud.aws.s3.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalStorageModeException extends MelodyException {

	private static final long serialVersionUID = 3634788709709700351L;

	public IllegalStorageModeException(String msg) {
		super(msg);
	}

	public IllegalStorageModeException(Throwable cause) {
		super(cause);
	}

	public IllegalStorageModeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
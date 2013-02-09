package com.wat.melody.core.nativeplugin.synchronize.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalLockIdException extends MelodyException {

	private static final long serialVersionUID = -389644684016806644L;

	public IllegalLockIdException() {
		super();
	}

	public IllegalLockIdException(String msg) {
		super(msg);
	}

	public IllegalLockIdException(Throwable cause) {
		super(cause);
	}

	public IllegalLockIdException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
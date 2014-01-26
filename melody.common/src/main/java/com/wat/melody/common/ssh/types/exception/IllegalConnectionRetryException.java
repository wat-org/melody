package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalConnectionRetryException extends MelodyException {

	private static final long serialVersionUID = -4365658776985454905L;

	public IllegalConnectionRetryException(String msg) {
		super(msg);
	}

	public IllegalConnectionRetryException(Throwable cause) {
		super(cause);
	}

	public IllegalConnectionRetryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
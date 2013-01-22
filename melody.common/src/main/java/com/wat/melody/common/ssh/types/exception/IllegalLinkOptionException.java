package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalLinkOptionException extends MelodyException {

	private static final long serialVersionUID = -439052267794870342L;

	public IllegalLinkOptionException() {
		super();
	}

	public IllegalLinkOptionException(String msg) {
		super(msg);
	}

	public IllegalLinkOptionException(Throwable cause) {
		super(cause);
	}

	public IllegalLinkOptionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
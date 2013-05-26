package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TemplatingException extends MelodyException {

	private static final long serialVersionUID = -187654436757532322L;

	public TemplatingException(String msg) {
		super(msg);
	}

	public TemplatingException(Throwable cause) {
		super(cause);
	}

	public TemplatingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
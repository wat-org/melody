package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalAccessException extends MelodyException {

	private static final long serialVersionUID = -3214321446579432249L;

	public IllegalAccessException() {
		super();
	}

	public IllegalAccessException(String msg) {
		super(msg);
	}

	public IllegalAccessException(Throwable cause) {
		super(cause);
	}

	public IllegalAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

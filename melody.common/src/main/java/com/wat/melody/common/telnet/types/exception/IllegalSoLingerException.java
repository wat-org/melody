package com.wat.melody.common.telnet.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalSoLingerException extends MelodyException {

	private static final long serialVersionUID = -1867533214245325435L;

	public IllegalSoLingerException(String msg) {
		super(msg);
	}

	public IllegalSoLingerException(Throwable cause) {
		super(cause);
	}

	public IllegalSoLingerException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIcmpTypeException extends MelodyException {

	private static final long serialVersionUID = -3876865765345279249L;

	public IllegalIcmpTypeException() {
		super();
	}

	public IllegalIcmpTypeException(String msg) {
		super(msg);
	}

	public IllegalIcmpTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalIcmpTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

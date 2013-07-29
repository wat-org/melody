package com.wat.melody.common.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalScopesException extends MelodyException {

	private static final long serialVersionUID = -8798765623245654349L;

	public IllegalScopesException(String msg) {
		super(msg);
	}

	public IllegalScopesException(Throwable cause) {
		super(cause);
	}

	public IllegalScopesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.common.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalScopeException extends MelodyException {

	private static final long serialVersionUID = -6325667097095654349L;

	public IllegalScopeException(String msg) {
		super(msg);
	}

	public IllegalScopeException(Throwable cause) {
		super(cause);
	}

	public IllegalScopeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
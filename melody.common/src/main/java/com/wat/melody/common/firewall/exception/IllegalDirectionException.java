package com.wat.melody.common.firewall.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDirectionException extends MelodyException {

	private static final long serialVersionUID = -3576323676832455249L;

	public IllegalDirectionException(String msg) {
		super(msg);
	}

	public IllegalDirectionException(Throwable cause) {
		super(cause);
	}

	public IllegalDirectionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

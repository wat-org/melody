package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProtocolException extends MelodyException {

	private static final long serialVersionUID = -3742148613234532249L;

	public IllegalProtocolException() {
		super();
	}

	public IllegalProtocolException(String msg) {
		super(msg);
	}

	public IllegalProtocolException(Throwable cause) {
		super(cause);
	}

	public IllegalProtocolException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

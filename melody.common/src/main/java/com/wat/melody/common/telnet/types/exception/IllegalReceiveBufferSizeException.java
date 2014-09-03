package com.wat.melody.common.telnet.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalReceiveBufferSizeException extends MelodyException {

	private static final long serialVersionUID = -3667533214245325435L;

	public IllegalReceiveBufferSizeException(String msg) {
		super(msg);
	}

	public IllegalReceiveBufferSizeException(Throwable cause) {
		super(cause);
	}

	public IllegalReceiveBufferSizeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
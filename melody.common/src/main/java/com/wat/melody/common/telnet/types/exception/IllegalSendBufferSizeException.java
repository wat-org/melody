package com.wat.melody.common.telnet.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalSendBufferSizeException extends MelodyException {

	private static final long serialVersionUID = -2467533214245325435L;

	public IllegalSendBufferSizeException(String msg) {
		super(msg);
	}

	public IllegalSendBufferSizeException(Throwable cause) {
		super(cause);
	}

	public IllegalSendBufferSizeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
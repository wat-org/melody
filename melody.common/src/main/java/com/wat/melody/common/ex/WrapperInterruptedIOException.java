package com.wat.melody.common.ex;

import java.io.InterruptedIOException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperInterruptedIOException extends InterruptedIOException {

	private static final long serialVersionUID = -2878551423245647614L;

	public WrapperInterruptedIOException(String msg) {
		super(msg);
	}

	public WrapperInterruptedIOException(Throwable cause) {
		super(null);
		initCause(cause);
	}

	public WrapperInterruptedIOException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

}
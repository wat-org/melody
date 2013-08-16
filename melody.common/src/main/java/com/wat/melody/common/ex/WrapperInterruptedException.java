package com.wat.melody.common.ex;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperInterruptedException extends InterruptedException {

	private static final long serialVersionUID = -6989878652124536814L;

	public WrapperInterruptedException(String msg) {
		super(msg);
	}

	public WrapperInterruptedException(Throwable cause) {
		super(null);
		initCause(cause);
	}

	public WrapperInterruptedException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

}
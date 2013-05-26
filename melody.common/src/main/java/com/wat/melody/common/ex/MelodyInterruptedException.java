package com.wat.melody.common.ex;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MelodyInterruptedException extends InterruptedException {

	private static final long serialVersionUID = -6989878652124536814L;

	public MelodyInterruptedException(String msg) {
		super(msg);
	}

	public MelodyInterruptedException(Throwable cause) {
		super(null);
		initCause(cause);
	}

	public MelodyInterruptedException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

}
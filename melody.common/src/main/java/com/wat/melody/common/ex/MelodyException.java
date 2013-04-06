package com.wat.melody.common.ex;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MelodyException extends Exception {

	/*
	 * TODO : change it to an interface
	 */
	private static final long serialVersionUID = -1184066155132415814L;

	public MelodyException() {
		super();
	}

	public MelodyException(String msg) {
		super(msg);
	}

	public MelodyException(Throwable cause) {
		super(null, cause);
	}

	public MelodyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.core.nativeplugin.synchronize.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalMaxParException extends MelodyException {

	private static final long serialVersionUID = -489644684016806644L;

	public IllegalMaxParException() {
		super();
	}

	public IllegalMaxParException(String msg) {
		super(msg);
	}

	public IllegalMaxParException(Throwable cause) {
		super(cause);
	}

	public IllegalMaxParException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
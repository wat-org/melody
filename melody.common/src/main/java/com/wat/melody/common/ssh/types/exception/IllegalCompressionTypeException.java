package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalCompressionTypeException extends MelodyException {

	private static final long serialVersionUID = -5432298782665529965L;

	public IllegalCompressionTypeException() {
		super();
	}

	public IllegalCompressionTypeException(String msg) {
		super(msg);
	}

	public IllegalCompressionTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalCompressionTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
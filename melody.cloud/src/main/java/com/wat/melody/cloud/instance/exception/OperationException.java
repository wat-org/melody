package com.wat.melody.cloud.instance.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class OperationException extends MelodyException {

	private static final long serialVersionUID = -234576345399879809L;

	public OperationException() {
		super();
	}

	public OperationException(String msg) {
		super(msg);
	}

	public OperationException(Throwable cause) {
		super(cause);
	}

	public OperationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
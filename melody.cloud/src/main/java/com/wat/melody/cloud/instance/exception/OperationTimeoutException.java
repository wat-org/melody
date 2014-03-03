package com.wat.melody.cloud.instance.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class OperationTimeoutException extends OperationException {

	private static final long serialVersionUID = -908876542414357609L;

	public OperationTimeoutException(String msg) {
		super(msg);
	}

	public OperationTimeoutException(Throwable cause) {
		super(cause);
	}

	public OperationTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
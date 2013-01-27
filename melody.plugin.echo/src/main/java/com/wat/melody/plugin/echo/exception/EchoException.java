package com.wat.melody.plugin.echo.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class EchoException extends TaskException {

	private static final long serialVersionUID = -7407890905648241668L;

	public EchoException() {
		super();
	}

	public EchoException(String msg) {
		super(msg);
	}

	public EchoException(Throwable cause) {
		super(cause);
	}

	public EchoException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
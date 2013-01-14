package com.wat.melody.common.ssh.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IncorrectCredentialsException extends SshSessionException {

	private static final long serialVersionUID = -2945679655497676554L;

	public IncorrectCredentialsException() {
		super();
	}

	public IncorrectCredentialsException(String msg) {
		super(msg);
	}

	public IncorrectCredentialsException(Throwable cause) {
		super(cause);
	}

	public IncorrectCredentialsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

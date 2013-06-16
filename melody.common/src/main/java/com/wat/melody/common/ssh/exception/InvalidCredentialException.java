package com.wat.melody.common.ssh.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InvalidCredentialException extends SshSessionException {

	private static final long serialVersionUID = -2945679655497676554L;

	public InvalidCredentialException(String msg) {
		super(msg);
	}

	public InvalidCredentialException(Throwable cause) {
		super(cause);
	}

	public InvalidCredentialException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
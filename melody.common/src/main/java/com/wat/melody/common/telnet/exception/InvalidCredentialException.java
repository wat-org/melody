package com.wat.melody.common.telnet.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InvalidCredentialException extends TelnetSessionException {

	private static final long serialVersionUID = -2948978077654325454L;

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
package com.wat.melody.plugin.ssh.common.jsch;

import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IncorrectCredentialsException extends SshException {

	private static final long serialVersionUID = -2987434586437897875L;

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

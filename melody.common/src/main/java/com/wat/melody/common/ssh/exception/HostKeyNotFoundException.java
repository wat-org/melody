package com.wat.melody.common.ssh.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class HostKeyNotFoundException extends SshSessionException {

	private static final long serialVersionUID = -3876543345678352434L;

	public HostKeyNotFoundException(String msg) {
		super(msg);
	}

	public HostKeyNotFoundException(Throwable cause) {
		super(cause);
	}

	public HostKeyNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
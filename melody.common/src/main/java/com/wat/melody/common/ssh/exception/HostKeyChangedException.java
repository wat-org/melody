package com.wat.melody.common.ssh.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class HostKeyChangedException extends SshSessionException {

	private static final long serialVersionUID = -6745434657897676554L;

	public HostKeyChangedException(String msg) {
		super(msg);
	}

	public HostKeyChangedException(Throwable cause) {
		super(cause);
	}

	public HostKeyChangedException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
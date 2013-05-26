package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshSessionException extends MelodyException {

	private static final long serialVersionUID = -187654436787636985L;

	public SshSessionException(String msg) {
		super(msg);
	}

	public SshSessionException(Throwable cause) {
		super(cause);
	}

	public SshSessionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
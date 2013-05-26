package com.wat.melody.plugin.ssh.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshException extends TaskException {

	private static final long serialVersionUID = -4894613206084760465L;

	public SshException(String msg) {
		super(msg);
	}

	public SshException(Throwable cause) {
		super(cause);
	}

	public SshException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
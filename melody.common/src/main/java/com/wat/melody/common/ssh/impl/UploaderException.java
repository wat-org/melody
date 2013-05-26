package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.ssh.exception.SshSessionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UploaderException extends SshSessionException {

	private static final long serialVersionUID = -2376809764497676654L;

	public UploaderException(String msg) {
		super(msg);
	}

	public UploaderException(Throwable cause) {
		super(cause);
	}

	public UploaderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

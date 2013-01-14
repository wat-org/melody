package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalSshSessionConfigurationException extends MelodyException {

	private static final long serialVersionUID = -5435648967622678085L;

	public IllegalSshSessionConfigurationException() {
		super();
	}

	public IllegalSshSessionConfigurationException(String msg) {
		super(msg);
	}

	public IllegalSshSessionConfigurationException(Throwable cause) {
		super(cause);
	}

	public IllegalSshSessionConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
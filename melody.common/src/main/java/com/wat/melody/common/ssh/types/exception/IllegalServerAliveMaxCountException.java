package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalServerAliveMaxCountException extends MelodyException {

	private static final long serialVersionUID = -1133593963741240905L;

	public IllegalServerAliveMaxCountException() {
		super();
	}

	public IllegalServerAliveMaxCountException(String msg) {
		super(msg);
	}

	public IllegalServerAliveMaxCountException(Throwable cause) {
		super(cause);
	}

	public IllegalServerAliveMaxCountException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
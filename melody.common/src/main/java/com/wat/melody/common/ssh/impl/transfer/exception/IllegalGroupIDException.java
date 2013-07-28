package com.wat.melody.common.ssh.impl.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalGroupIDException extends MelodyException {

	private static final long serialVersionUID = -8676564432134334572L;

	public IllegalGroupIDException(String msg) {
		super(msg);
	}

	public IllegalGroupIDException(Throwable cause) {
		super(cause);
	}

	public IllegalGroupIDException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
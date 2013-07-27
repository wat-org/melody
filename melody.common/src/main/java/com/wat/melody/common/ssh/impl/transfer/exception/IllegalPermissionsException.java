package com.wat.melody.common.ssh.impl.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPermissionsException extends MelodyException {

	private static final long serialVersionUID = -6543122347878787772L;

	public IllegalPermissionsException(String msg) {
		super(msg);
	}

	public IllegalPermissionsException(Throwable cause) {
		super(cause);
	}

	public IllegalPermissionsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
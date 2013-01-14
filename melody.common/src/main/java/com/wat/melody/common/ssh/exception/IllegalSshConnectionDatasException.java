package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalSshConnectionDatasException extends MelodyException {

	private static final long serialVersionUID = -187753523870976535L;

	public IllegalSshConnectionDatasException() {
		super();
	}

	public IllegalSshConnectionDatasException(String msg) {
		super(msg);
	}

	public IllegalSshConnectionDatasException(Throwable cause) {
		super(cause);
	}

	public IllegalSshConnectionDatasException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

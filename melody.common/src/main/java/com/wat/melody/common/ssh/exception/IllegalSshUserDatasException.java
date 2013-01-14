package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalSshUserDatasException extends MelodyException {

	private static final long serialVersionUID = -686434736578987965L;

	public IllegalSshUserDatasException() {
		super();
	}

	public IllegalSshUserDatasException(String msg) {
		super(msg);
	}

	public IllegalSshUserDatasException(Throwable cause) {
		super(cause);
	}

	public IllegalSshUserDatasException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
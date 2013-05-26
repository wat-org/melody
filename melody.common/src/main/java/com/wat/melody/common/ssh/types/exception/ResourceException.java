package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourceException extends MelodyException {

	private static final long serialVersionUID = -2131244434324870342L;

	public ResourceException(String msg) {
		super(msg);
	}

	public ResourceException(Throwable cause) {
		super(cause);
	}

	public ResourceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
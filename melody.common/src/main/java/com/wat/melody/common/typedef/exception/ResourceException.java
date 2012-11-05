package com.wat.melody.common.typedef.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourceException extends MelodyException {

	private static final long serialVersionUID = -2131244434324870342L;

	public ResourceException() {
		super();
	}

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
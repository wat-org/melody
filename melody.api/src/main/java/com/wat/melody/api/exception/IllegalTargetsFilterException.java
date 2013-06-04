package com.wat.melody.api.exception;

import com.wat.melody.common.filter.exception.IllegalFilterException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalTargetsFilterException extends IllegalFilterException {

	private static final long serialVersionUID = -3364876876087402681L;

	public IllegalTargetsFilterException(String msg) {
		super(msg);
	}

	public IllegalTargetsFilterException(Throwable cause) {
		super(cause);
	}

	public IllegalTargetsFilterException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.api.exception;

import com.wat.melody.common.filter.exception.IllegalFilterException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalResourcesFilterException extends IllegalFilterException {

	private static final long serialVersionUID = -2498706541654205817L;

	public IllegalResourcesFilterException(String msg) {
		super(msg);
	}

	public IllegalResourcesFilterException(Throwable cause) {
		super(cause);
	}

	public IllegalResourcesFilterException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
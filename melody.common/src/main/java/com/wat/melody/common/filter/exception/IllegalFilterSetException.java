package com.wat.melody.common.filter.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalFilterSetException extends MelodyException {

	private static final long serialVersionUID = -1345334245678665665L;

	public IllegalFilterSetException(String msg) {
		super(msg);
	}

	public IllegalFilterSetException(Throwable cause) {
		super(cause);
	}

	public IllegalFilterSetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
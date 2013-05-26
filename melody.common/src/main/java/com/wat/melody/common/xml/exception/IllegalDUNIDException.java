package com.wat.melody.common.xml.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDUNIDException extends MelodyException {

	private static final long serialVersionUID = -1879879875634534570L;

	public IllegalDUNIDException(String msg) {
		super(msg);
	}

	public IllegalDUNIDException(Throwable cause) {
		super(cause);
	}

	public IllegalDUNIDException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.common.xml.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NoSuchDUNIDException extends MelodyException {

	private static final long serialVersionUID = -5793245723345769615L;

	public NoSuchDUNIDException() {
		super();
	}

	public NoSuchDUNIDException(String msg) {
		super(msg);
	}

	public NoSuchDUNIDException(Throwable cause) {
		super(cause);
	}

	public NoSuchDUNIDException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.melody.cloud.instance.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalInstanceDatasException extends MelodyException {

	private static final long serialVersionUID = -121387769832798536L;

	public IllegalInstanceDatasException(String msg) {
		super(msg);
	}

	public IllegalInstanceDatasException(Throwable cause) {
		super(cause);
	}

	public IllegalInstanceDatasException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
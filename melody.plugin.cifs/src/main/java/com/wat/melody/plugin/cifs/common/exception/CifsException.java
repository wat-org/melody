package com.wat.melody.plugin.cifs.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CifsException extends TaskException {

	private static final long serialVersionUID = -7654525648974760465L;

	public CifsException(String msg) {
		super(msg);
	}

	public CifsException(Throwable cause) {
		super(cause);
	}

	public CifsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
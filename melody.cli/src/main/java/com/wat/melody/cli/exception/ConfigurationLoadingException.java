package com.wat.melody.cli.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConfigurationLoadingException extends MelodyException {

	private static final long serialVersionUID = 1597821304849804204L;

	public ConfigurationLoadingException() {
		super();
	}

	public ConfigurationLoadingException(String msg) {
		super(msg);
	}

	public ConfigurationLoadingException(Throwable cause) {
		super(cause);
	}

	public ConfigurationLoadingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
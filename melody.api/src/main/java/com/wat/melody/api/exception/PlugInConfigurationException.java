package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PlugInConfigurationException extends MelodyException {

	private static final long serialVersionUID = -1212312432454365602L;

	public PlugInConfigurationException(String msg) {
		super(msg);
	}

	public PlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public PlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
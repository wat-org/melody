package com.wat.melody.plugin.telnet.common.exception;

import com.wat.melody.api.exception.PlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetPlugInConfigurationException extends
		PlugInConfigurationException {

	private static final long serialVersionUID = -897675434532532543L;

	public TelnetPlugInConfigurationException(String msg) {
		super(msg);
	}

	public TelnetPlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public TelnetPlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
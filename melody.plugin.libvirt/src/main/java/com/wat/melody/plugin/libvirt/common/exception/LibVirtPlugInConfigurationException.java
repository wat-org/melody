package com.wat.melody.plugin.libvirt.common.exception;

import com.wat.melody.api.exception.PlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtPlugInConfigurationException extends
		PlugInConfigurationException {

	private static final long serialVersionUID = 983212356657786543L;

	public LibVirtPlugInConfigurationException() {
		super();
	}

	public LibVirtPlugInConfigurationException(String msg) {
		super(msg);
	}

	public LibVirtPlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public LibVirtPlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

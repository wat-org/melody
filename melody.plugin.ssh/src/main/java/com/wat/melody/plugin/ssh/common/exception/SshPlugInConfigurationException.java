package com.wat.melody.plugin.ssh.common.exception;

import com.wat.melody.api.exception.PlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshPlugInConfigurationException extends
		PlugInConfigurationException {

	private static final long serialVersionUID = -154395565432121233L;

	public SshPlugInConfigurationException(String msg) {
		super(msg);
	}

	public SshPlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public SshPlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
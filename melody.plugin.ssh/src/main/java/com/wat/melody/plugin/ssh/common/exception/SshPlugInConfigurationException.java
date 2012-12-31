package com.wat.melody.plugin.ssh.common.exception;

import com.wat.melody.api.exception.PluginConfigurationException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConfigurationException extends PluginConfigurationException {

	private static final long serialVersionUID = -154395565432121233L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

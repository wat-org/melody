package com.wat.melody.plugin.aws.ec2.common.exception;

import com.wat.melody.api.exception.PluginConfigurationException;

public class ConfigurationException extends PluginConfigurationException {

	private static final long serialVersionUID = 983212356657786543L;

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

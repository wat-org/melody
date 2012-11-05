package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class PluginConfigurationException extends MelodyException {

	private static final long serialVersionUID = -1212312432454365602L;

	public PluginConfigurationException() {
		super();
	}

	public PluginConfigurationException(String msg) {
		super(msg);
	}

	public PluginConfigurationException(Throwable cause) {
		super(cause);
	}

	public PluginConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
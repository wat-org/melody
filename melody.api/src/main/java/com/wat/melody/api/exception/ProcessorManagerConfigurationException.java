package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class ProcessorManagerConfigurationException extends MelodyException {

	private static final long serialVersionUID = 6549841630840684077L;

	public ProcessorManagerConfigurationException() {
		super();
	}

	public ProcessorManagerConfigurationException(String msg) {
		super(msg);
	}

	public ProcessorManagerConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
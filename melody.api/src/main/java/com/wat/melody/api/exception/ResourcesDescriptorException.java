package com.wat.melody.api.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class ResourcesDescriptorException extends MelodyException {

	private static final long serialVersionUID = -2498745678654205817L;

	public ResourcesDescriptorException() {
		super();
	}

	public ResourcesDescriptorException(String msg) {
		super(msg);
	}

	public ResourcesDescriptorException(Throwable cause) {
		super(cause);
	}

	public ResourcesDescriptorException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
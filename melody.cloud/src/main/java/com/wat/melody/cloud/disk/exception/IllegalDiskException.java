package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalDiskException extends MelodyException {

	private static final long serialVersionUID = -268994524679997771L;

	public IllegalDiskException() {
		super();
	}

	public IllegalDiskException(String msg) {
		super(msg);
	}

	public IllegalDiskException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

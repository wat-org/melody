package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class IllegalDiskListException extends MelodyException {

	private static final long serialVersionUID = -268994527679997771L;

	public IllegalDiskListException() {
		super();
	}

	public IllegalDiskListException(String msg) {
		super(msg);
	}

	public IllegalDiskListException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskListException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

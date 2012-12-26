package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.utils.exception.MelodyException;

public class DiskException extends MelodyException {

	private static final long serialVersionUID = -168997524679997771L;

	public DiskException() {
		super();
	}

	public DiskException(String msg) {
		super(msg);
	}

	public DiskException(Throwable cause) {
		super(cause);
	}

	public DiskException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

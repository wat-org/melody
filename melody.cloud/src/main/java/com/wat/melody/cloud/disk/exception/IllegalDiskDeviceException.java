package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.ex.MelodyException;

public class IllegalDiskDeviceException extends MelodyException {

	private static final long serialVersionUID = -268994524679997771L;

	public IllegalDiskDeviceException() {
		super();
	}

	public IllegalDiskDeviceException(String msg) {
		super(msg);
	}

	public IllegalDiskDeviceException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskDeviceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

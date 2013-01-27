package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceException extends MelodyException {

	private static final long serialVersionUID = -168997524679997771L;

	public DiskDeviceException() {
		super();
	}

	public DiskDeviceException(String msg) {
		super(msg);
	}

	public DiskDeviceException(Throwable cause) {
		super(cause);
	}

	public DiskDeviceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

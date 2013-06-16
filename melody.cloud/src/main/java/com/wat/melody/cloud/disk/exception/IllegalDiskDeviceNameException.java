package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDiskDeviceNameException extends MelodyException {

	private static final long serialVersionUID = -875425435435429871L;

	public IllegalDiskDeviceNameException(String msg) {
		super(msg);
	}

	public IllegalDiskDeviceNameException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskDeviceNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
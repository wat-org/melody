package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDiskDeviceListException extends MelodyException {

	private static final long serialVersionUID = -268994527679997771L;

	public IllegalDiskDeviceListException(String msg) {
		super(msg);
	}

	public IllegalDiskDeviceListException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskDeviceListException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
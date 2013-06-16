package com.wat.melody.cloud.disk.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalDiskDeviceSizeException extends MelodyException {

	private static final long serialVersionUID = -498798794399809871L;

	public IllegalDiskDeviceSizeException(String msg) {
		super(msg);
	}

	public IllegalDiskDeviceSizeException(Throwable cause) {
		super(cause);
	}

	public IllegalDiskDeviceSizeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
package com.wat.cloud.aws.ec2.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalVolumeStateException extends MelodyException {

	private static final long serialVersionUID = -654654322574326576L;

	public IllegalVolumeStateException() {
		super();
	}

	public IllegalVolumeStateException(String msg) {
		super(msg);
	}

	public IllegalVolumeStateException(Throwable cause) {
		super(cause);
	}

	public IllegalVolumeStateException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
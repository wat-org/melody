package com.wat.melody.common.log.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalLogThresholdException extends MelodyException {

	private static final long serialVersionUID = -8436032157406321050L;

	public IllegalLogThresholdException(String msg) {
		super(msg);
	}

	public IllegalLogThresholdException(Throwable cause) {
		super(cause);
	}

	public IllegalLogThresholdException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
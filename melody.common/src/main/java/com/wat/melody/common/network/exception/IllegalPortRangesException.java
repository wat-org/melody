package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPortRangesException extends MelodyException {

	private static final long serialVersionUID = -6332148643239879792L;

	public IllegalPortRangesException() {
		super();
	}

	public IllegalPortRangesException(String msg) {
		super(msg);
	}

	public IllegalPortRangesException(Throwable cause) {
		super(cause);
	}

	public IllegalPortRangesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

package com.wat.melody.common.network.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalIpRangesException extends MelodyException {

	private static final long serialVersionUID = -2094535783270632632L;

	public IllegalIpRangesException() {
		super();
	}

	public IllegalIpRangesException(String msg) {
		super(msg);
	}

	public IllegalIpRangesException(Throwable cause) {
		super(cause);
	}

	public IllegalIpRangesException(String msg, Throwable cause) {
		super(msg, cause);
	}

}